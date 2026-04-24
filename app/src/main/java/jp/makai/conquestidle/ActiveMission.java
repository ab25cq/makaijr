package com.makaijr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class ActiveMission {
    final String villageId;
    final String timeId;
    final List<String> partyMonsterIds;
    final int partyAttack;
    final int partyDefense;
    final int partyMaxHp;
    final int initialVillageProgress;
    final int elapsedSeconds;
    final int partyHp;
    final int remainingVillageControl;
    final int earnedControl;
    final int tribute;
    final long seed;
    final long startedAtEpochMillis;
    final long lastUpdatedAtEpochMillis;
    final long expectedCompletionAtEpochMillis;
    final boolean replayMode;
    final String lastLogLine;

    ActiveMission(
            String villageId,
            String timeId,
            List<String> partyMonsterIds,
            int partyAttack,
            int partyDefense,
            int partyMaxHp,
            int initialVillageProgress,
            int elapsedSeconds,
            int partyHp,
            int remainingVillageControl,
            int earnedControl,
            int tribute,
            long seed,
            long startedAtEpochMillis,
            long lastUpdatedAtEpochMillis,
            long expectedCompletionAtEpochMillis,
            boolean replayMode,
            String lastLogLine
    ) {
        this.villageId = villageId;
        this.timeId = timeId;
        this.partyMonsterIds = new ArrayList<>(partyMonsterIds);
        this.partyAttack = partyAttack;
        this.partyDefense = partyDefense;
        this.partyMaxHp = partyMaxHp;
        this.initialVillageProgress = initialVillageProgress;
        this.elapsedSeconds = elapsedSeconds;
        this.partyHp = partyHp;
        this.remainingVillageControl = remainingVillageControl;
        this.earnedControl = earnedControl;
        this.tribute = tribute;
        this.seed = seed;
        this.startedAtEpochMillis = startedAtEpochMillis;
        this.lastUpdatedAtEpochMillis = lastUpdatedAtEpochMillis;
        this.expectedCompletionAtEpochMillis = expectedCompletionAtEpochMillis;
        this.replayMode = replayMode;
        this.lastLogLine = lastLogLine;
    }

    JSONObject toJson() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("villageId", villageId);
        root.put("timeId", timeId);
        root.put("partyMonsterIds", new JSONArray(partyMonsterIds));
        root.put("partyAttack", partyAttack);
        root.put("partyDefense", partyDefense);
        root.put("partyMaxHp", partyMaxHp);
        root.put("initialVillageProgress", initialVillageProgress);
        root.put("elapsedSeconds", elapsedSeconds);
        root.put("partyHp", partyHp);
        root.put("remainingVillageControl", remainingVillageControl);
        root.put("earnedControl", earnedControl);
        root.put("tribute", tribute);
        root.put("seed", seed);
        root.put("startedAtEpochMillis", startedAtEpochMillis);
        root.put("lastUpdatedAtEpochMillis", lastUpdatedAtEpochMillis);
        root.put("expectedCompletionAtEpochMillis", expectedCompletionAtEpochMillis);
        root.put("replayMode", replayMode);
        root.put("lastLogLine", lastLogLine);
        return root;
    }

    static ActiveMission fromJson(JSONObject root) {
        JSONArray partyArray = root.optJSONArray("partyMonsterIds");
        List<String> partyMonsterIds = new ArrayList<>();
        if (partyArray != null) {
            for (int i = 0; i < partyArray.length(); i++) {
                String monsterId = partyArray.optString(i, "");
                if (!monsterId.isEmpty()) {
                    partyMonsterIds.add(monsterId);
                }
            }
        }

        return new ActiveMission(
                root.optString("villageId", GameData.VILLAGES[0].id),
                root.optString("timeId", GameData.TIME_OPTIONS[0].id),
                partyMonsterIds,
                Math.max(0, root.optInt("partyAttack", 0)),
                Math.max(0, root.optInt("partyDefense", 0)),
                Math.max(1, root.optInt("partyMaxHp", 1)),
                Math.max(0, root.optInt("initialVillageProgress", 0)),
                Math.max(0, root.optInt("elapsedSeconds", 0)),
                Math.max(0, root.optInt("partyHp", 0)),
                Math.max(0, root.optInt("remainingVillageControl", 0)),
                Math.max(0, root.optInt("earnedControl", 0)),
                Math.max(0, root.optInt("tribute", 0)),
                root.optLong("seed", 1L),
                Math.max(0L, root.optLong("startedAtEpochMillis", 0L)),
                Math.max(0L, root.optLong("lastUpdatedAtEpochMillis", 0L)),
                Math.max(0L, root.optLong("expectedCompletionAtEpochMillis", 0L)),
                root.optBoolean("replayMode", false),
                root.optString("lastLogLine", "")
        );
    }
}
