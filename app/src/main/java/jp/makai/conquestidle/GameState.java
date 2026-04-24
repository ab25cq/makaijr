package com.makaijr;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class GameState {
    private static final String MINIMUM_MONSTER_ID = "slime";
    private static final String PREF_NAME = "makai_command";
    private static final String KEY_MONSTER = "selected_monster";
    private static final String KEY_MONSTERS = "selected_monsters";
    private static final String KEY_MONSTER_ROSTER = "monster_roster";
    private static final String KEY_VILLAGE = "selected_village";
    private static final String KEY_TIME = "selected_time";
    private static final String KEY_DEMON_LORD_EXP = "demon_lord_exp";
    private static final String KEY_PROGRESS_PREFIX = "village_progress_";
    private static final String KEY_MONSTER_EXP_PREFIX = "monster_exp_";
    private static final String KEY_ACTIVE_MISSION = "active_mission";
    private static final String KEY_PENDING_MISSION_REPORT = "pending_mission_report";

    private final SharedPreferences preferences;
    private final Context appContext;

    GameState(Context context) {
        appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        ensureDefaults();
    }

    Context getAppContext() {
        return appContext;
    }

    private void ensureDefaults() {
        SharedPreferences.Editor editor = preferences.edit();
        if (!preferences.contains(KEY_MONSTER_ROSTER)) {
            editor.putString(KEY_MONSTER_ROSTER, buildInitialRosterJson());
        }
        if (!preferences.contains(KEY_VILLAGE)) {
            editor.putString(KEY_VILLAGE, GameData.VILLAGES[0].id);
        }
        if (!preferences.contains(KEY_TIME)) {
            editor.putString(KEY_TIME, GameData.TIME_OPTIONS[0].id);
        }
        if (!preferences.contains(KEY_DEMON_LORD_EXP)) {
            editor.putInt(KEY_DEMON_LORD_EXP, 0);
        }
        editor.apply();
        ensureMinimumRoster();
        persistSelectedMonsters(getSelectedMonsterIds());
    }

    private String buildInitialRosterJson() {
        List<OwnedMonster> roster = new ArrayList<>();
        roster.add(createMinimumMonster(0));
        return serializeRoster(roster);
    }

    private String buildLegacyRosterJson(int demonLordExp, JSONObject monsterExp, List<String> selectedMonsterIds, JSONObject villageProgress) {
        List<OwnedMonster> roster = new ArrayList<>();
        int demonLordLevel = Progression.getDemonLordLevelForExp(demonLordExp);
        int finalDungeonProgress = villageProgress == null ? 0 : Math.max(0, villageProgress.optInt("abyss99", 0));
        int finalDungeonRequiredControl = GameData.getVillage("abyss99").requiredControl;

        for (Monster monster : GameData.MONSTERS) {
            boolean legacyUnlocked = monster.requiredDemonLordLevel <= demonLordLevel;
            if ("abyssdragon".equals(monster.id)) {
                legacyUnlocked = legacyUnlocked && finalDungeonProgress >= finalDungeonRequiredControl;
            }
            int exp = monsterExp == null ? 0 : Math.max(0, monsterExp.optInt(monster.id, 0));
            boolean previouslySelected = selectedMonsterIds.contains(monster.id);
            if (legacyUnlocked || exp > 0 || previouslySelected) {
                roster.add(new OwnedMonster(buildMonsterInstanceId(monster.id, roster.size()), monster.id, exp));
            }
        }
        if (roster.isEmpty()) {
            roster.add(createMinimumMonster(0));
        }
        return serializeRoster(roster);
    }

    private boolean legacySelectionContains(String monsterId) {
        String single = preferences.getString(KEY_MONSTER, "");
        if (monsterId.equals(single)) {
            return true;
        }
        String multi = preferences.getString(KEY_MONSTERS, "");
        if (multi == null || multi.isEmpty()) {
            return false;
        }
        String[] parts = multi.split(",");
        for (String part : parts) {
            if (monsterId.equals(part.trim())) {
                return true;
            }
        }
        return false;
    }

    List<OwnedMonster> getOwnedMonsters() {
        return loadRoster();
    }

    boolean hasOwnedMonsters() {
        return !loadRoster().isEmpty();
    }

    OwnedMonster getOwnedMonster(String instanceIdOrMonsterId) {
        if (instanceIdOrMonsterId == null || instanceIdOrMonsterId.isEmpty()) {
            return null;
        }
        List<OwnedMonster> roster = loadRoster();
        for (OwnedMonster ownedMonster : roster) {
            if (ownedMonster.instanceId.equals(instanceIdOrMonsterId)) {
                return ownedMonster;
            }
        }
        for (OwnedMonster ownedMonster : roster) {
            if (ownedMonster.monsterId.equals(instanceIdOrMonsterId)) {
                return ownedMonster;
            }
        }
        return null;
    }

    String getSelectedMonsterId() {
        List<String> selectedIds = getSelectedMonsterIds();
        return selectedIds.isEmpty() ? "" : selectedIds.get(0);
    }

    void setSelectedMonsterId(String monsterId) {
        List<String> monsterIds = new ArrayList<>();
        monsterIds.add(monsterId);
        setSelectedMonsterIds(monsterIds);
    }

    List<String> getSelectedMonsterIds() {
        String stored = preferences.getString(
                KEY_MONSTERS,
                preferences.getString(KEY_MONSTER, "")
        );
        List<OwnedMonster> roster = loadRoster();
        int partySlots = getAvailablePartySlots();
        List<String> selectedIds = new ArrayList<>();
        if (stored != null && !stored.isEmpty()) {
            String[] parts = stored.split(",");
            for (String part : parts) {
                String sanitized = findOwnedMonsterInstanceId(part.trim(), roster);
                if (!sanitized.isEmpty()
                        && !selectedIds.contains(sanitized)
                        && selectedIds.size() < partySlots) {
                    selectedIds.add(sanitized);
                }
            }
        }
        if (selectedIds.isEmpty() && !roster.isEmpty()) {
            selectedIds.add(roster.get(0).instanceId);
        }
        return selectedIds;
    }

    void setSelectedMonsterIds(List<String> monsterIds) {
        List<OwnedMonster> roster = loadRoster();
        int partySlots = getAvailablePartySlots();
        List<String> sanitized = new ArrayList<>();
        for (String monsterId : monsterIds) {
            String instanceId = findOwnedMonsterInstanceId(monsterId, roster);
            if (!instanceId.isEmpty()
                    && !sanitized.contains(instanceId)
                    && sanitized.size() < partySlots) {
                sanitized.add(instanceId);
            }
        }
        if (sanitized.isEmpty() && !roster.isEmpty()) {
            sanitized.add(roster.get(0).instanceId);
        }
        persistSelectedMonsters(sanitized);
    }

    String getSelectedVillageId() {
        return sanitizeVillageId(
                preferences.getString(KEY_VILLAGE, GameData.VILLAGES[0].id),
                getDemonLordLevel()
        );
    }

    void setSelectedVillageId(String villageId) {
        preferences.edit()
                .putString(KEY_VILLAGE, sanitizeVillageId(villageId, getDemonLordLevel()))
                .apply();
    }

    String getSelectedTimeId() {
        return preferences.getString(KEY_TIME, GameData.TIME_OPTIONS[0].id);
    }

    void setSelectedTimeId(String timeId) {
        preferences.edit().putString(KEY_TIME, timeId).apply();
    }

    int getVillageProgress(String villageId) {
        return preferences.getInt(KEY_PROGRESS_PREFIX + villageId, 0);
    }

    void addVillageProgress(String villageId, int points, int maxPoints) {
        int current = getVillageProgress(villageId);
        int updated = Math.min(maxPoints, current + Math.max(points, 0));
        preferences.edit().putInt(KEY_PROGRESS_PREFIX + villageId, updated).apply();
    }

    List<OwnedMonster> grantVillageRecruitRewards(String villageId, int previousProgress, int updatedProgress) {
        Village village = GameData.getVillage(villageId);
        List<OwnedMonster> recruits = new ArrayList<>();
        if (updatedProgress <= previousProgress) {
            return recruits;
        }
        for (int i = 0; i < GameData.RECRUIT_PROGRESS_THRESHOLDS.length; i++) {
            int thresholdPercent = GameData.RECRUIT_PROGRESS_THRESHOLDS[i];
            int thresholdValue = (int) Math.ceil((village.requiredControl * thresholdPercent) / 100.0);
            if (previousProgress < thresholdValue && updatedProgress >= thresholdValue) {
                Monster recruit = GameData.getRecruitRewardMonster(village, i);
                recruits.add(recruitMonster(recruit.id));
            }
        }
        return recruits;
    }

    OwnedMonster recruitMonster(String monsterId) {
        List<OwnedMonster> roster = loadRoster();
        OwnedMonster recruit = new OwnedMonster(
                buildMonsterInstanceId(monsterId, roster.size()),
                monsterId,
                0
        );
        roster.add(recruit);
        saveRoster(roster);
        if (getSelectedMonsterIds().isEmpty()) {
            List<String> selectedIds = new ArrayList<>();
            selectedIds.add(recruit.instanceId);
            setSelectedMonsterIds(selectedIds);
        }
        return recruit;
    }

    void removeMonsters(List<String> instanceIds) {
        if (instanceIds == null || instanceIds.isEmpty()) {
            return;
        }
        List<OwnedMonster> roster = loadRoster();
        List<OwnedMonster> survivors = new ArrayList<>();
        for (OwnedMonster ownedMonster : roster) {
            if (!instanceIds.contains(ownedMonster.instanceId)) {
                survivors.add(ownedMonster);
            }
        }
        saveRoster(survivors);
        setSelectedMonsterIds(getSelectedMonsterIds());
    }

    int getMonsterExp(String instanceId) {
        OwnedMonster ownedMonster = getOwnedMonster(instanceId);
        return ownedMonster == null ? 0 : ownedMonster.exp;
    }

    void addMonsterExp(String instanceId, int exp) {
        if (exp <= 0) {
            return;
        }
        List<OwnedMonster> roster = loadRoster();
        for (int i = 0; i < roster.size(); i++) {
            OwnedMonster ownedMonster = roster.get(i);
            if (ownedMonster.instanceId.equals(instanceId)) {
                roster.set(i, new OwnedMonster(ownedMonster.instanceId, ownedMonster.monsterId, ownedMonster.exp + exp));
                saveRoster(roster);
                return;
            }
        }
    }

    int getMonsterLevel(String instanceId) {
        return Progression.getMonsterLevelForExp(getMonsterExp(instanceId));
    }

    int getDemonLordExp() {
        return preferences.getInt(KEY_DEMON_LORD_EXP, 0);
    }

    void addDemonLordExp(int exp) {
        if (exp <= 0) {
            return;
        }
        preferences.edit().putInt(KEY_DEMON_LORD_EXP, getDemonLordExp() + exp).apply();
    }

    int getDemonLordLevel() {
        return Progression.getDemonLordLevelForExp(getDemonLordExp());
    }

    int getAvailablePartySlots() {
        return Progression.getPartySizeForDemonLordLevel(getDemonLordLevel());
    }

    ActiveMission getActiveMission() {
        String stored = preferences.getString(KEY_ACTIVE_MISSION, "");
        if (stored == null || stored.isEmpty()) {
            return null;
        }
        try {
            return ActiveMission.fromJson(new JSONObject(stored));
        } catch (JSONException e) {
            preferences.edit().remove(KEY_ACTIVE_MISSION).apply();
            return null;
        }
    }

    void setActiveMission(ActiveMission mission) {
        try {
            preferences.edit().putString(KEY_ACTIVE_MISSION, mission.toJson().toString()).apply();
        } catch (JSONException e) {
            preferences.edit().remove(KEY_ACTIVE_MISSION).apply();
        }
    }

    void clearActiveMission() {
        preferences.edit().remove(KEY_ACTIVE_MISSION).apply();
    }

    String getPendingMissionReport() {
        return preferences.getString(KEY_PENDING_MISSION_REPORT, "");
    }

    void setPendingMissionReport(String report) {
        preferences.edit().putString(KEY_PENDING_MISSION_REPORT, report).apply();
    }

    String consumePendingMissionReport() {
        String report = getPendingMissionReport();
        preferences.edit().remove(KEY_PENDING_MISSION_REPORT).apply();
        return report;
    }

    void resetAll() {
        preferences.edit().clear().apply();
        ensureDefaults();
    }

    String exportToJson() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("selectedMonsters", new JSONArray(getSelectedMonsterIds()));
        root.put("selectedVillage", getSelectedVillageId());
        root.put("selectedTime", getSelectedTimeId());
        root.put("demonLordExp", getDemonLordExp());

        JSONObject villageProgress = new JSONObject();
        for (Village village : GameData.VILLAGES) {
            villageProgress.put(village.id, getVillageProgress(village.id));
        }
        root.put("villageProgress", villageProgress);

        JSONArray monsterRoster = new JSONArray();
        for (OwnedMonster ownedMonster : getOwnedMonsters()) {
            monsterRoster.put(ownedMonster.toJson());
        }
        root.put("monsterRoster", monsterRoster);

        ActiveMission activeMission = getActiveMission();
        if (activeMission != null) {
            root.put("activeMission", activeMission.toJson());
        }
        String pendingMissionReport = getPendingMissionReport();
        if (!pendingMissionReport.isEmpty()) {
            root.put("pendingMissionReport", pendingMissionReport);
        }
        return root.toString(2);
    }

    void importFromJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();

        int demonLordExp = Math.max(0, root.optInt("demonLordExp", 0));
        editor.putInt(KEY_DEMON_LORD_EXP, demonLordExp);

        JSONObject villageProgress = root.optJSONObject("villageProgress");
        if (villageProgress != null) {
            for (Village village : GameData.VILLAGES) {
                int progress = Math.max(0, villageProgress.optInt(village.id, 0));
                editor.putInt(KEY_PROGRESS_PREFIX + village.id, Math.min(progress, village.requiredControl));
            }
        }

        JSONArray monsterRoster = root.optJSONArray("monsterRoster");
        JSONObject monsterExp = root.optJSONObject("monsterExp");

        String villageId = sanitizeVillageId(
                root.optString("selectedVillage", GameData.VILLAGES[0].id),
                Progression.getDemonLordLevelForExp(demonLordExp)
        );
        String timeId = sanitizeTimeId(root.optString("selectedTime", GameData.TIME_OPTIONS[0].id));

        List<String> selectedMonsterIds = new ArrayList<>();
        JSONArray selectedMonsters = root.optJSONArray("selectedMonsters");
        if (selectedMonsters != null) {
            for (int i = 0; i < selectedMonsters.length(); i++) {
                String monsterId = selectedMonsters.optString(i, "");
                if (!monsterId.isEmpty() && !selectedMonsterIds.contains(monsterId)) {
                    selectedMonsterIds.add(monsterId);
                }
            }
        } else {
            String singleMonsterId = root.optString("selectedMonster", "");
            if (!singleMonsterId.isEmpty()) {
                selectedMonsterIds.add(singleMonsterId);
            }
        }

        if (monsterRoster != null) {
            editor.putString(KEY_MONSTER_ROSTER, monsterRoster.toString());
        } else {
            if (monsterExp != null) {
                for (Monster monster : GameData.MONSTERS) {
                    editor.putInt(KEY_MONSTER_EXP_PREFIX + monster.id, Math.max(0, monsterExp.optInt(monster.id, 0)));
                }
            }
            editor.putString(
                    KEY_MONSTER_ROSTER,
                    buildLegacyRosterJson(demonLordExp, monsterExp, selectedMonsterIds, villageProgress)
            );
        }

        editor.putString(KEY_VILLAGE, villageId);
        editor.putString(KEY_TIME, timeId);
        JSONObject activeMission = root.optJSONObject("activeMission");
        if (activeMission != null) {
            editor.putString(KEY_ACTIVE_MISSION, activeMission.toString());
        }
        String pendingMissionReport = root.optString("pendingMissionReport", "");
        if (!pendingMissionReport.isEmpty()) {
            editor.putString(KEY_PENDING_MISSION_REPORT, pendingMissionReport);
        }
        editor.apply();

        ensureDefaults();
        setSelectedMonsterIds(selectedMonsterIds);
    }

    private List<OwnedMonster> loadRoster() {
        String stored = preferences.getString(KEY_MONSTER_ROSTER, "[]");
        List<OwnedMonster> roster = new ArrayList<>();
        if (stored == null || stored.isEmpty()) {
            return roster;
        }
        try {
            JSONArray array = new JSONArray(stored);
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.optJSONObject(i);
                if (entry == null) {
                    continue;
                }
                OwnedMonster ownedMonster = OwnedMonster.fromJson(entry);
                if (!ownedMonster.instanceId.isEmpty() && containsMonsterId(ownedMonster.monsterId)) {
                    roster.add(ownedMonster);
                }
            }
        } catch (JSONException ignored) {
            return new ArrayList<>();
        }
        return roster;
    }

    private void saveRoster(List<OwnedMonster> roster) {
        List<OwnedMonster> sanitized = new ArrayList<>(roster);
        if (sanitized.isEmpty()) {
            sanitized.add(createMinimumMonster(0));
        }
        preferences.edit().putString(KEY_MONSTER_ROSTER, serializeRoster(sanitized)).apply();
    }

    private String serializeRoster(List<OwnedMonster> roster) {
        JSONArray array = new JSONArray();
        for (OwnedMonster ownedMonster : roster) {
            try {
                array.put(ownedMonster.toJson());
            } catch (JSONException ignored) {
            }
        }
        return array.toString();
    }

    private void persistSelectedMonsters(List<String> selectedIds) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < selectedIds.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(selectedIds.get(i));
        }
        preferences.edit()
                .putString(KEY_MONSTERS, builder.toString())
                .putString(KEY_MONSTER, selectedIds.isEmpty() ? "" : selectedIds.get(0))
                .apply();
    }

    private String findOwnedMonsterInstanceId(String token, List<OwnedMonster> roster) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        for (OwnedMonster ownedMonster : roster) {
            if (ownedMonster.instanceId.equals(token)) {
                return ownedMonster.instanceId;
            }
        }
        for (OwnedMonster ownedMonster : roster) {
            if (ownedMonster.monsterId.equals(token)) {
                return ownedMonster.instanceId;
            }
        }
        return "";
    }

    private String buildMonsterInstanceId(String monsterId, int salt) {
        return monsterId + "_" + Long.toHexString(System.currentTimeMillis()) + "_" + Integer.toHexString(salt);
    }

    private OwnedMonster createMinimumMonster(int salt) {
        return new OwnedMonster(buildMonsterInstanceId(MINIMUM_MONSTER_ID, salt), MINIMUM_MONSTER_ID, 0);
    }

    private void ensureMinimumRoster() {
        List<OwnedMonster> roster = loadRoster();
        if (roster.isEmpty()) {
            roster.add(createMinimumMonster(0));
            saveRoster(roster);
        }
    }

    private boolean containsMonsterId(String monsterId) {
        for (Monster monster : GameData.MONSTERS) {
            if (monster.id.equals(monsterId)) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeVillageId(String villageId, int demonLordLevel) {
        for (Village village : GameData.VILLAGES) {
            if (village.id.equals(villageId) && GameData.isVillageUnlocked(village, demonLordLevel)) {
                return villageId;
            }
        }
        return getFallbackVillageId(demonLordLevel);
    }

    private String getFallbackVillageId(int demonLordLevel) {
        for (Village village : GameData.VILLAGES) {
            if (GameData.isVillageUnlocked(village, demonLordLevel)) {
                return village.id;
            }
        }
        return GameData.VILLAGES[0].id;
    }

    private String sanitizeTimeId(String timeId) {
        for (TimeOption timeOption : GameData.TIME_OPTIONS) {
            if (timeOption.id.equals(timeId)) {
                return timeId;
            }
        }
        return GameData.TIME_OPTIONS[0].id;
    }
}
