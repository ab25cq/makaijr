package com.makaijr;

final class Progression {
    private Progression() {
    }

    static int getMonsterLevelForExp(int exp) {
        int level = 1;
        int remaining = Math.max(0, exp);
        while (remaining >= getMonsterExpForNextLevel(level)) {
            remaining -= getMonsterExpForNextLevel(level);
            level++;
        }
        return level;
    }

    static int getDemonLordLevelForExp(int exp) {
        int level = 1;
        int remaining = Math.max(0, exp);
        while (remaining >= getDemonLordExpForNextLevel(level)) {
            remaining -= getDemonLordExpForNextLevel(level);
            level++;
        }
        return level;
    }

    static int getMonsterExpForNextLevel(int level) {
        return 35 + ((level - 1) * 20);
    }

    static int getDemonLordExpForNextLevel(int level) {
        return 35 + ((level - 1) * 20);
    }

    static int getExpIntoCurrentMonsterLevel(int exp) {
        int remaining = Math.max(0, exp);
        int level = 1;
        while (remaining >= getMonsterExpForNextLevel(level)) {
            remaining -= getMonsterExpForNextLevel(level);
            level++;
        }
        return remaining;
    }

    static int getExpIntoCurrentDemonLordLevel(int exp) {
        int remaining = Math.max(0, exp);
        int level = 1;
        while (remaining >= getDemonLordExpForNextLevel(level)) {
            remaining -= getDemonLordExpForNextLevel(level);
            level++;
        }
        return remaining;
    }

    static int getPartySizeForDemonLordLevel(int level) {
        if (level >= 5) {
            return 4;
        }
        if (level >= 4) {
            return 3;
        }
        if (level >= 2) {
            return 2;
        }
        return 1;
    }

    static int getMonsterAttack(Monster monster, int level) {
        return monster.attack + ((level - 1) * 3);
    }

    static int getMonsterDefense(Monster monster, int level) {
        return monster.defense + ((level - 1) * 2);
    }

    static int getMonsterHp(Monster monster, int level) {
        return monster.maxHp + ((level - 1) * 12);
    }
}
