package com.makaijr;

import org.json.JSONException;
import org.json.JSONObject;

final class OwnedMonster {
    final String instanceId;
    final String monsterId;
    final int exp;

    OwnedMonster(String instanceId, String monsterId, int exp) {
        this.instanceId = instanceId;
        this.monsterId = monsterId;
        this.exp = Math.max(0, exp);
    }

    Monster getMonster() {
        return GameData.getMonster(monsterId);
    }

    int getLevel() {
        return Progression.getMonsterLevelForExp(exp);
    }

    String getDisplayName() {
        String suffix = instanceId;
        if (suffix.length() > 4) {
            suffix = suffix.substring(suffix.length() - 4);
        }
        return getMonster().name + " #" + suffix;
    }

    JSONObject toJson() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("instanceId", instanceId);
        root.put("monsterId", monsterId);
        root.put("exp", exp);
        return root;
    }

    static OwnedMonster fromJson(JSONObject root) {
        return new OwnedMonster(
                root.optString("instanceId", ""),
                root.optString("monsterId", GameData.MONSTERS[0].id),
                Math.max(0, root.optInt("exp", 0))
        );
    }
}
