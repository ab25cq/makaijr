package com.makaijr;

final class Village {
    final String id;
    final String name;
    final int requiredControl;
    final int attack;
    final int defense;
    final int requiredDemonLordLevel;
    final int dungeonFloors;
    final int referenceClearSeconds;
    final String description;

    Village(
            String id,
            String name,
            int requiredControl,
            int attack,
            int defense,
            int requiredDemonLordLevel,
            int dungeonFloors,
            int referenceClearSeconds,
            String description
    ) {
        this.id = id;
        this.name = name;
        this.requiredControl = requiredControl;
        this.attack = attack;
        this.defense = defense;
        this.requiredDemonLordLevel = requiredDemonLordLevel;
        this.dungeonFloors = dungeonFloors;
        this.referenceClearSeconds = referenceClearSeconds;
        this.description = description;
    }

    boolean isDungeon() {
        return dungeonFloors > 0;
    }
}
