package com.makaijr;

import java.util.Locale;

final class GameText {
    private GameText() {
    }

    static boolean isJapanese() {
        return Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage());
    }

    static String text(String english, String japanese) {
        return isJapanese() ? japanese : english;
    }

    static String demonLord() {
        return text("Demon Lord", "魔王");
    }

    static String monsterName(Monster monster) {
        if (isJapanese()) {
            return monster.name;
        }
        switch (monster.id) {
            case "imp":
                return "Scout Imp";
            case "slime":
                return "Miasma Slime";
            case "gargoyle":
                return "Gargoyle Guard";
            case "drake":
                return "Young Drake";
            case "gremlin":
                return "Saboteur Engineer";
            case "bat":
                return "Nightwing Bat";
            case "hobgoblin":
                return "Hob Captain";
            case "hellhound":
                return "Hellhound";
            case "ghoul":
                return "Grave Ghoul";
            case "basilisk":
                return "Young Basilisk";
            case "specter":
                return "Blueflame Specter";
            case "mimic":
                return "Spoils Mimic";
            case "orc":
                return "Orc Company";
            case "wraith":
                return "Abyss Wraith";
            case "harpy":
                return "Cliff Harpy";
            case "lizardman":
                return "Marsh Lancer";
            case "golem":
                return "Obsidian Golem";
            case "arachne":
                return "Arachne Huntress";
            case "minotaur":
                return "Maze Minotaur";
            case "vampire":
                return "Night Banquet Vampire";
            case "wyvern":
                return "Venom Wyvern";
            case "dullahan":
                return "Headless Knight";
            case "chimera":
                return "Three-Headed Chimera";
            case "darkmage":
                return "Abyss Mage";
            case "ogre":
                return "Ogre General";
            case "manticore":
                return "Manticore";
            case "cerberus":
                return "Cerberus";
            case "deathknight":
                return "Death Knight";
            case "archdemon":
                return "Archdemon Commander";
            case "abyssdragon":
                return "Abyss Dragon";
            default:
                return monster.name;
        }
    }

    static String monsterTagline(Monster monster) {
        if (isJapanese()) {
            return monster.tagline;
        }
        switch (monster.id) {
            case "slime":
                return "A durable starter";
            case "abyssdragon":
                return "A late-game conqueror";
            default:
                return "A battle-ready unit";
        }
    }

    static String monsterDescription(Monster monster) {
        if (isJapanese()) {
            return monster.description;
        }
        switch (monster.id) {
            case "slime":
                return "A reliable starter unit that survives long missions and slowly raises conquest progress.";
            case "abyssdragon":
                return "A final reward unit that joins after the last dungeon is conquered.";
            default:
                return "A unit suited for village conquest. Raise its level before pushing into harder areas.";
        }
    }

    static String villageName(Village village) {
        if (isJapanese()) {
            return village.name;
        }
        switch (village.id) {
            case "harvest":
                return "Wheatfield Village";
            case "quarry":
                return "Blackstone Quarry";
            case "sanctum":
                return "Holy Bell Village";
            case "marsh":
                return "Mistmarsh Village";
            case "forge":
                return "Ironflame Forge";
            case "canal":
                return "White Canal Market";
            case "citadel":
                return "Cliffside Citadel";
            case "cathedral":
                return "Chained Cathedral";
            case "imperial":
                return "Royal Outpost";
            case "abyss99":
                return "Abyss Lockfront, 99 Floors";
            default:
                return village.name;
        }
    }

    static String villageDescription(Village village) {
        if (isJapanese()) {
            return village.description;
        }
        switch (village.id) {
            case "harvest":
                return "A light first target. It is easy to occupy with a short mission.";
            case "quarry":
                return "A quarry with walls and watchtowers. It punishes weak forces early.";
            case "abyss99":
                return "The final 99-floor dungeon. It requires massive time, levels, and surviving forces.";
            default:
                return "A harder target with stronger resistance. Level up before committing long missions.";
        }
    }

    static String timeLabel(TimeOption option) {
        if (isJapanese()) {
            return option.label;
        }
        switch (option.id) {
            case "15m":
                return "15 min";
            case "30m":
                return "30 min";
            case "45m":
                return "45 min";
            case "1h":
                return "1 hour";
            case "2h":
                return "2 hours";
            case "3h":
                return "3 hours";
            default:
                return option.label;
        }
    }

    static String timeDescription(TimeOption option) {
        if (isJapanese()) {
            return option.description;
        }
        switch (option.id) {
            case "15m":
                return "A short and safer scouting mission.";
            case "30m":
                return "A balanced real-time mission.";
            case "45m":
                return "A longer push that is still reasonably stable.";
            case "1h":
                return "A full assault. Rewards improve, but damage starts to matter.";
            case "2h":
                return "A risky long mission for tougher targets.";
            case "3h":
                return "The longest mission. Underleveled forces can be wiped out.";
            default:
                return option.description;
        }
    }

    static String missionWord() {
        return text("mission", "作戦");
    }

    static String unitsSuffix(int count) {
        return text(count + " units", count + "体");
    }
}
