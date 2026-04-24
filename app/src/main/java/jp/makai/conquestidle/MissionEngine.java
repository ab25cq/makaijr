package com.makaijr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Locale;

final class MissionEngine {
    private static final double FULL_ATTRITION_SECONDS = 6.0 * 60.0 * 60.0;

    private MissionEngine() {
    }

    static boolean startMission(GameState gameState, long nowMillis) {
        if (gameState.getActiveMission() != null) {
            return false;
        }

        Village village = GameData.getVillage(gameState.getSelectedVillageId());
        if (!GameData.isVillageUnlocked(village, gameState.getDemonLordLevel())) {
            return false;
        }

        List<String> partyIds = gameState.getSelectedMonsterIds();
        if (partyIds.isEmpty()) {
            return false;
        }
        int partyAttack = 0;
        int partyDefense = 0;
        int partyMaxHp = 0;
        for (String monsterId : partyIds) {
            OwnedMonster ownedMonster = gameState.getOwnedMonster(monsterId);
            if (ownedMonster == null) {
                continue;
            }
            Monster monster = ownedMonster.getMonster();
            int level = ownedMonster.getLevel();
            partyAttack += Progression.getMonsterAttack(monster, level);
            partyDefense += Progression.getMonsterDefense(monster, level);
            partyMaxHp += Progression.getMonsterHp(monster, level);
        }
        if (partyMaxHp <= 0) {
            return false;
        }

        int storedVillageProgress = gameState.getVillageProgress(village.id);
        boolean replayMode = storedVillageProgress >= village.requiredControl;
        int missionStartProgress = replayMode ? 0 : storedVillageProgress;
        long missionSeed = nowMillis ^ ((long) village.id.hashCode() << 32);
        ActiveMission mission = new ActiveMission(
                village.id,
                gameState.getSelectedTimeId(),
                partyIds,
                partyAttack,
                partyDefense,
                partyMaxHp,
                missionStartProgress,
                0,
                partyMaxHp,
                Math.max(0, village.requiredControl - missionStartProgress),
                0,
                0,
                missionSeed,
                nowMillis,
                nowMillis,
                forecastCompletionTime(nowMillis, new ActiveMission(
                        village.id,
                        gameState.getSelectedTimeId(),
                        partyIds,
                        partyAttack,
                        partyDefense,
                        partyMaxHp,
                        missionStartProgress,
                        0,
                        partyMaxHp,
                        Math.max(0, village.requiredControl - missionStartProgress),
                        0,
                        0,
                        missionSeed,
                        nowMillis,
                        nowMillis,
                        0L,
                        replayMode,
                        ""
                )),
                replayMode,
                "作戦開始。司令部へ戻っても経過時間ぶん自動で侵攻が進む。"
        );
        gameState.setActiveMission(mission);
        MissionNotificationReceiver.schedule(gameState.getAppContext(), mission);
        return true;
    }

    static int syncActiveMission(GameState gameState, long nowMillis) {
        ActiveMission mission = gameState.getActiveMission();
        if (mission == null) {
            return 0;
        }

        TimeOption timeOption = GameData.getTimeOption(mission.timeId);
        long elapsedMillis = Math.max(0L, nowMillis - mission.lastUpdatedAtEpochMillis);
        int secondsToProcess = (int) Math.min(Integer.MAX_VALUE, elapsedMillis / 1000L);
        if (secondsToProcess <= 0) {
            return 0;
        }

        ActiveMission updated = mission;
        long processedUntil = mission.lastUpdatedAtEpochMillis;
        for (int i = 0; i < secondsToProcess; i++) {
            processedUntil += 1000L;
            updated = advanceOneSecond(updated);
            if (updated.remainingVillageControl <= 0) {
                completeMission(gameState, updated, buildVictoryMessage(GameData.getVillage(updated.villageId)));
                return i + 1;
            }
            if (updated.partyHp <= 0) {
                completeMission(gameState, updated, "モンスターが撤退した。今回の制圧進行だけ持ち帰る。");
                return i + 1;
            }
            if (updated.elapsedSeconds >= timeOption.durationSeconds) {
                completeMission(gameState, updated, "作戦時間が終了した。現時点の戦果を確定する。");
                return i + 1;
            }
        }

        ActiveMission persisted = new ActiveMission(
                updated.villageId,
                updated.timeId,
                updated.partyMonsterIds,
                updated.partyAttack,
                updated.partyDefense,
                updated.partyMaxHp,
                updated.initialVillageProgress,
                updated.elapsedSeconds,
                updated.partyHp,
                updated.remainingVillageControl,
                updated.earnedControl,
                updated.tribute,
                updated.seed,
                updated.startedAtEpochMillis,
                processedUntil,
                updated.expectedCompletionAtEpochMillis,
                updated.replayMode,
                updated.lastLogLine
        );
        gameState.setActiveMission(persisted);
        return secondsToProcess;
    }

    static String formatDuration(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int hours = safeSeconds / 3600;
        int minutes = (safeSeconds % 3600) / 60;
        int seconds = safeSeconds % 60;
        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    static String formatDateTime(long epochMillis) {
        if (epochMillis <= 0L) {
            return "-";
        }
        return new SimpleDateFormat("M/d HH:mm", Locale.JAPAN).format(new Date(epochMillis));
    }

    private static long forecastCompletionTime(long startedAtEpochMillis, ActiveMission mission) {
        ActiveMission cursor = mission;
        TimeOption timeOption = GameData.getTimeOption(mission.timeId);
        while (true) {
            cursor = advanceOneSecond(cursor);
            if (cursor.remainingVillageControl <= 0
                    || cursor.partyHp <= 0
                    || cursor.elapsedSeconds >= timeOption.durationSeconds) {
                return startedAtEpochMillis + (cursor.elapsedSeconds * 1000L);
            }
        }
    }

    private static ActiveMission advanceOneSecond(ActiveMission mission) {
        Village village = GameData.getVillage(mission.villageId);
        int nextElapsed = mission.elapsedSeconds + 1;
        int actingMonsterIndex = deterministicRoll(mission.seed, nextElapsed, 0, 0, mission.partyMonsterIds.size() - 1);
        Monster actingMonster = getMonsterForPartyToken(mission.partyMonsterIds.get(actingMonsterIndex));
        int dealt = cumulativeControlGain(mission, nextElapsed) - cumulativeControlGain(mission, mission.elapsedSeconds);
        int taken = cumulativeDamageTaken(mission, nextElapsed) - cumulativeDamageTaken(mission, mission.elapsedSeconds);

        return new ActiveMission(
                mission.villageId,
                mission.timeId,
                mission.partyMonsterIds,
                mission.partyAttack,
                mission.partyDefense,
                mission.partyMaxHp,
                mission.initialVillageProgress,
                nextElapsed,
                Math.max(0, mission.partyHp - taken),
                Math.max(0, mission.remainingVillageControl - dealt),
                mission.earnedControl + dealt,
                mission.tribute + (dealt * 3),
                mission.seed,
                mission.startedAtEpochMillis,
                mission.lastUpdatedAtEpochMillis,
                mission.expectedCompletionAtEpochMillis,
                mission.replayMode,
                String.format(
                        Locale.US,
                        "%02d:%02d:%02d  %s が 制圧 +%d、%s の抵抗で部隊被害 +%d。",
                        nextElapsed / 3600,
                        (nextElapsed % 3600) / 60,
                        nextElapsed % 60,
                        actingMonster.name,
                        dealt,
                        village.name,
                        taken
                )
        );
    }

    private static int cumulativeControlGain(ActiveMission mission, int elapsedSeconds) {
        Village village = GameData.getVillage(mission.villageId);
        double conquestFactor = getConquestFactor(mission, village);
        double rawGain = (village.requiredControl * conquestFactor * elapsedSeconds) / village.referenceClearSeconds;
        return Math.max(0, Math.min(village.requiredControl, (int) Math.floor(rawGain)));
    }

    private static int cumulativeDamageTaken(ActiveMission mission, int elapsedSeconds) {
        Village village = GameData.getVillage(mission.villageId);
        double attritionFactor = getAttritionFactor(mission, village);
        double durationRiskFactor = getDurationRiskFactor(GameData.getTimeOption(mission.timeId));
        double rawDamage = (mission.partyMaxHp * attritionFactor * durationRiskFactor * elapsedSeconds)
                / FULL_ATTRITION_SECONDS;
        return Math.max(0, Math.min(mission.partyMaxHp, (int) Math.floor(rawDamage)));
    }

    private static double getConquestFactor(ActiveMission mission, Village village) {
        double offenseScore = mission.partyAttack + (mission.partyDefense * 0.75);
        double resistanceScore = (village.attack * 1.8) + (village.defense * 1.6) + 8.0;
        double ratio = offenseScore / Math.max(1.0, resistanceScore);
        double preparednessRatio = getPreparednessRatio(mission, village);
        double preparednessPenalty = Math.pow(clamp(preparednessRatio, 0.05, 1.00), 1.90);
        return clamp(ratio * preparednessPenalty, 0.01, 1.00);
    }

    private static double getAttritionFactor(ActiveMission mission, Village village) {
        double pressureScore = (village.attack * 2.0) + (village.defense * 0.8);
        double enduranceScore = mission.partyDefense + (mission.partyMaxHp * 0.12);
        double ratio = pressureScore / Math.max(1.0, enduranceScore);
        double preparednessRatio = getPreparednessRatio(mission, village);
        double underpreparedPenalty = preparednessRatio >= 1.0
                ? 1.0
                : 1.0 + Math.pow((1.0 - preparednessRatio) * 4.6, 3.2);
        return clamp(ratio * underpreparedPenalty, 0.25, 14.00);
    }

    private static double getPreparednessRatio(ActiveMission mission, Village village) {
        double partyPower = (mission.partyAttack * 1.2)
                + mission.partyDefense
                + (mission.partyMaxHp * 0.34);
        double expectedPower = 28.0
                + (village.attack * 3.1)
                + (village.defense * 3.0)
                + (village.requiredDemonLordLevel * 18.0)
                + (village.requiredControl * 0.06);
        return partyPower / Math.max(1.0, expectedPower);
    }

    private static double getDurationRiskFactor(TimeOption timeOption) {
        switch (timeOption.id) {
            case "15m":
                return 0.55;
            case "30m":
                return 0.75;
            case "45m":
                return 0.95;
            case "1h":
                return 1.15;
            case "2h":
                return 1.80;
            case "3h":
                return 3.00;
            default:
                return 1.00;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int deterministicRoll(long seed, int tick, int salt, int min, int max) {
        long value = seed + (tick * 0x9E3779B97F4A7C15L) + (salt * 0xC2B2AE3D27D4EB4FL);
        value ^= (value >>> 33);
        value *= 0xff51afd7ed558ccdL;
        value ^= (value >>> 33);
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= (value >>> 33);
        int range = max - min + 1;
        return min + (int) Math.floorMod(value, range);
    }

    private static String buildVictoryMessage(Village village) {
        if (village.isDungeon()) {
            return "第99階まで踏破した。難攻不落の最終ダンジョンがついに陥落した。";
        }
        return "村の支配率が 100% に到達した。魔界の旗が立った。";
    }

    private static void completeMission(GameState gameState, ActiveMission mission, String message) {
        MissionNotificationReceiver.cancel(gameState.getAppContext());
        Village village = GameData.getVillage(mission.villageId);
        int previousProgress = gameState.getVillageProgress(village.id);
        if (!mission.replayMode) {
            gameState.addVillageProgress(village.id, mission.earnedControl, village.requiredControl);
        }
        int missionFinalProgress = Math.min(village.requiredControl, mission.initialVillageProgress + mission.earnedControl);
        int finalProgress = mission.replayMode ? village.requiredControl : gameState.getVillageProgress(village.id);
        boolean clearedMission = missionFinalProgress >= village.requiredControl;
        boolean newlyConquered = !mission.replayMode
                && mission.initialVillageProgress < village.requiredControl
                && finalProgress >= village.requiredControl;
        boolean annihilated = mission.partyHp <= 0;
        double progressRewardMultiplier = mission.earnedControl <= 0
                ? 0.0
                : ((double) mission.earnedControl / village.requiredControl);
        if (annihilated) {
            progressRewardMultiplier *= 0.5;
        }
        List<OwnedMonster> recruits = mission.replayMode
                ? new ArrayList<>()
                : gameState.grantVillageRecruitRewards(village.id, previousProgress, finalProgress);

        StringBuilder builder = new StringBuilder();
        builder.append(message)
                .append("\n戦果: 制圧進行 +")
                .append(mission.earnedControl)
                .append(" / 供物 ")
                .append(mission.tribute);

        if (newlyConquered) {
            builder.append("\n").append(village.name).append(" は征服済みになった。");
            appendRewardSummary(builder, gameState, village, mission.partyMonsterIds, progressRewardMultiplier, annihilated);
        } else if (clearedMission) {
            if (mission.replayMode) {
                builder.append("\n").append(village.name).append(" を再攻略した。");
            } else {
                builder.append("\n").append(village.name).append(" は征服済みになった。");
            }
            appendRewardSummary(builder, gameState, village, mission.partyMonsterIds, progressRewardMultiplier, annihilated);
        } else {
            builder.append("\n現在の進行: ").append(GameData.getVillageProgressLabel(village, missionFinalProgress));
            appendRewardSummary(builder, gameState, village, mission.partyMonsterIds, progressRewardMultiplier, annihilated);
        }
        appendRecruitSummary(builder, recruits);
        if (annihilated) {
            appendCasualtySummary(builder, gameState, mission.partyMonsterIds);
            gameState.removeMonsters(mission.partyMonsterIds);
        }

        gameState.clearActiveMission();
        gameState.setPendingMissionReport(builder.toString());
    }

    private static void appendRewardSummary(
            StringBuilder builder,
            GameState gameState,
            Village village,
            List<String> partyMonsterIds,
            double rewardMultiplier,
            boolean annihilated
    ) {
        String rewardSummary = applyConquestRewards(gameState, village, partyMonsterIds, rewardMultiplier, annihilated);
        if (!rewardSummary.isEmpty()) {
            builder.append("\n").append(rewardSummary);
        }
    }

    private static String applyConquestRewards(
            GameState gameState,
            Village village,
            List<String> partyMonsterIds,
            double rewardMultiplier,
            boolean annihilated
    ) {
        if (rewardMultiplier <= 0.0) {
            return "";
        }
        int demonExpReward = Math.max(1, (int) Math.floor(GameData.getDemonLordExpReward(village) * rewardMultiplier));
        int previousDemonLevel = gameState.getDemonLordLevel();
        gameState.addDemonLordExp(demonExpReward);
        int currentDemonLevel = gameState.getDemonLordLevel();

        StringBuilder builder = new StringBuilder();
        builder.append("征服報酬: 魔王 EXP +").append(demonExpReward);
        if (annihilated) {
            builder.append("  (全滅したため半減)");
        } else if (rewardMultiplier < 1.0) {
            builder.append("  (今回進めた制圧率ぶん)");
        }

        int monsterExpReward = Math.max(1, (int) Math.floor(GameData.getMonsterExpReward(village) * rewardMultiplier));
        for (String monsterId : partyMonsterIds) {
            OwnedMonster ownedMonster = gameState.getOwnedMonster(monsterId);
            if (ownedMonster == null) {
                continue;
            }
            Monster monster = ownedMonster.getMonster();
            int previousMonsterLevel = gameState.getMonsterLevel(ownedMonster.instanceId);
            gameState.addMonsterExp(ownedMonster.instanceId, monsterExpReward);
            int currentMonsterLevel = gameState.getMonsterLevel(ownedMonster.instanceId);
            builder.append("\n")
                    .append(ownedMonster.getDisplayName())
                    .append(" EXP +")
                    .append(monsterExpReward);
            if (currentMonsterLevel > previousMonsterLevel) {
                builder.append("  Lv").append(previousMonsterLevel).append(" -> Lv").append(currentMonsterLevel);
            }
        }

        if (currentDemonLevel > previousDemonLevel) {
            builder.append("\n魔王が Lv").append(previousDemonLevel).append(" から Lv").append(currentDemonLevel).append(" に上がった。");
            builder.append("\n編成上限は ").append(Progression.getPartySizeForDemonLordLevel(currentDemonLevel)).append(" 体。");
        }
        return builder.toString();
    }

    private static void appendRecruitSummary(StringBuilder builder, List<OwnedMonster> recruits) {
        if (recruits.isEmpty()) {
            return;
        }
        builder.append("\n新たな配下:");
        for (OwnedMonster recruit : recruits) {
            builder.append("\n")
                    .append(recruit.getDisplayName())
                    .append(" が加わった。");
        }
    }

    private static void appendCasualtySummary(StringBuilder builder, GameState gameState, List<String> partyMonsterIds) {
        if (partyMonsterIds.isEmpty()) {
            return;
        }
        builder.append("\n戦死した個体:");
        for (String monsterId : partyMonsterIds) {
            OwnedMonster ownedMonster = gameState.getOwnedMonster(monsterId);
            if (ownedMonster == null) {
                continue;
            }
            builder.append("\n").append(ownedMonster.getDisplayName());
        }
    }

    private static Monster getMonsterForPartyToken(String token) {
        if (token == null || token.isEmpty()) {
            return GameData.MONSTERS[0];
        }
        Monster direct = GameData.getMonster(token);
        if (direct.id.equals(token)) {
            return direct;
        }
        int separatorIndex = token.indexOf('_');
        if (separatorIndex > 0) {
            String monsterId = token.substring(0, separatorIndex);
            Monster fromPrefix = GameData.getMonster(monsterId);
            if (fromPrefix.id.equals(monsterId)) {
                return fromPrefix;
            }
        }
        return GameData.MONSTERS[0];
    }
}
