package com.makaijr;

final class Monster {
    final String id;
    final String name;
    final String tagline;
    final int requiredDemonLordLevel;
    final int attack;
    final int defense;
    final int maxHp;
    final String description;

    Monster(
            String id,
            String name,
            String tagline,
            int requiredDemonLordLevel,
            int attack,
            int defense,
            int maxHp,
            String description
    ) {
        this.id = id;
        this.name = name;
        this.tagline = tagline;
        this.requiredDemonLordLevel = requiredDemonLordLevel;
        this.attack = attack;
        this.defense = defense;
        this.maxHp = maxHp;
        this.description = description;
    }
}
