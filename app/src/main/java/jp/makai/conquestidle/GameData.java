package com.makaijr;

import java.util.ArrayList;
import java.util.List;

final class GameData {
    private static final String FINAL_DUNGEON_ID = "abyss99";
    static final int[] RECRUIT_PROGRESS_THRESHOLDS = new int[] {50, 100};

    static final Monster[] MONSTERS = new Monster[] {
            new Monster("imp", "小悪魔の斥候", "素早く削る先鋒", 1, 18, 8, 95, "足が速く、短時間の奇襲で真価を発揮する小型モンスター。"),
            new Monster("slime", "瘴気スライム", "しぶとい侵食役", 1, 14, 12, 125, "受けに強く、長い作戦でじわじわ村の支配率を伸ばす。"),
            new Monster("gargoyle", "ガーゴイル兵", "堅実な制圧担当", 1, 21, 14, 140, "石の身体で反撃に耐えつつ、安定して前線を押し上げる。"),
            new Monster("drake", "幼竜ドレイク", "高火力の征服者", 1, 28, 11, 120, "火力特化。短時間でも一気に制圧率を稼ぎやすい。"),
            new Monster("gremlin", "グレムリン工兵", "妨害と撹乱が得意", 1, 17, 10, 105, "罠と破壊工作で村の防衛線を崩す小柄な破壊屋。"),
            new Monster("bat", "夜翼バット", "空から削る奇襲役", 1, 20, 7, 92, "夜陰に紛れて急降下し、短期決戦で鋭く制圧を進める。"),

            new Monster("hobgoblin", "ホブゴブリン兵長", "前線をまとめる中堅", 2, 24, 15, 145, "統率に優れ、複数編成の安定感を引き上げる実戦派。"),
            new Monster("hellhound", "ヘルハウンド", "噛み砕く高速突撃", 2, 30, 11, 118, "灼熱の牙で村人を追い散らす、攻撃寄りの魔獣。"),
            new Monster("ghoul", "墓荒らしグール", "継戦力の高い死肉喰らい", 2, 22, 16, 150, "死臭をまとって進み、反撃を受けてもなかなか倒れない。"),
            new Monster("basilisk", "バジリスク幼体", "防衛線を鈍らせる蛇眼", 2, 27, 13, 128, "石化の魔眼で敵を竦ませ、突破口を作る。"),
            new Monster("specter", "青焔スペクター", "霊障を撒く攪乱役", 2, 25, 12, 116, "実体の薄い火霊。防壁の裏を抜けるように侵攻する。"),
            new Monster("mimic", "戦利品ミミック", "しぶとい待ち伏せ箱", 2, 23, 18, 158, "擬態からの奇襲と装甲で、意外な粘りを見せる。"),

            new Monster("orc", "オーク百人隊", "正面突破の主力歩兵", 3, 31, 18, 168, "力押しに優れた魔界の主力。長時間作戦で真価を出す。"),
            new Monster("wraith", "深淵レイス", "抵抗値を刈る怨霊", 3, 33, 12, 126, "村に染み込むように侵入し、支配率を素早く押し上げる。"),
            new Monster("harpy", "断崖ハーピー", "空襲と離脱の名手", 3, 29, 14, 132, "急襲を繰り返し、敵の反撃を散らす高機動型。"),
            new Monster("lizardman", "湿地のリザードマン", "崩れにくい槍兵", 3, 28, 19, 170, "長槍の壁を作り、部隊全体の耐久感を支える。"),
            new Monster("golem", "黒曜ゴーレム", "重装の制圧壁", 3, 26, 23, 215, "鈍いが硬い。長期占領で抵抗値を確実に削り切る。"),
            new Monster("arachne", "アラクネ狩猟姫", "捕縛から畳み掛ける", 3, 34, 13, 124, "糸で足を止め、一気に陣形を崩す処刑人。"),

            new Monster("minotaur", "迷宮ミノタウロス", "圧殺する突城槌", 4, 38, 20, 196, "壁ごと吹き飛ばす破城の怪物。重い一撃で村を黙らせる。"),
            new Monster("vampire", "夜宴のヴァンパイア", "吸収で伸びる支配者", 4, 36, 17, 166, "血を啜るたびに勢いを増し、戦線を掌握していく。"),
            new Monster("wyvern", "猛毒ワイバーン", "空中制圧の爪牙", 4, 40, 15, 158, "毒爪と滑空で防衛線の薄い場所を抉り取る。"),
            new Monster("dullahan", "デュラハン騎士", "規律正しい首なし騎兵", 4, 35, 22, 188, "恐怖と秩序を併せ持つ、攻防バランス型の騎士。"),
            new Monster("chimera", "三首キマイラ", "全距離対応の怪獣", 4, 42, 16, 176, "炎と牙と角で隙を与えず、どの村にも対応できる。"),
            new Monster("darkmage", "深淵の魔導兵", "術式で抵抗を溶かす", 4, 39, 14, 148, "制圧向きの呪詛を得意とする後衛型モンスター。"),

            new Monster("ogre", "巨腕オーガ将軍", "鈍重だが止まらない", 5, 44, 22, 228, "一歩進むたび地面を割り、村の士気を砕く巨将。"),
            new Monster("manticore", "マンティコア", "尾針で仕留める狩猟王", 5, 46, 18, 182, "獅子の膂力と毒針で、前線を一気に食い荒らす。"),
            new Monster("cerberus", "ケルベロス", "三頭で攻め立てる門番", 5, 48, 19, 190, "猛火の咆哮で守備隊を混乱させる地獄犬。"),
            new Monster("deathknight", "デスナイト", "制圧戦の完成形", 5, 41, 25, 210, "呪われた甲冑に身を包み、堅実に勝ち筋を積む。"),
            new Monster("archdemon", "上位魔将アークデーモン", "魔王軍の切り札", 5, 52, 20, 198, "高位悪魔の指揮官。単体でも部隊でも火力が高い。"),
            new Monster("abyssdragon", "奈落竜アビスドラゴン", "奈落制圧で加わる覇竜", 5, 56, 21, 205, "最終ダンジョンを制圧した時、魔王軍へ加わる終盤の切り札。")
    };

    static final Village[] VILLAGES = new Village[] {
            new Village("harvest", "麦穂の村", 120, 6, 7, 1, 0, 1200, "最初の足掛かりに向いた小村。守りは薄く、短時間でも落としやすい。"),
            new Village("quarry", "黒岩の採掘村", 280, 14, 18, 1, 0, 5400, "石壁と見張り台を備えた採掘拠点。序盤から急に粘る。"),
            new Village("sanctum", "聖鐘の村", 390, 18, 23, 2, 0, 10800, "鐘楼に守られた厄介な村。初期戦力では長時間の攻防になりやすい。"),
            new Village("marsh", "霧湿地の村", 540, 23, 30, 4, 0, 21600, "深い霧と沼地が侵攻を鈍らせる。継戦力の低い部隊は押し返されやすい。"),
            new Village("forge", "鉄火の鍛冶村", 700, 29, 38, 6, 0, 32400, "自警団の装備が厚く、中盤の壁として立ちはだかる鍛冶の村。"),
            new Village("canal", "白運河の交易村", 900, 36, 47, 9, 0, 43200, "補給線が強く、短時間ではほとんど崩れない富裕拠点。"),
            new Village("citadel", "断崖の城塞村", 1150, 44, 58, 12, 0, 64800, "崖上の砦と弩砲が厄介な山岳要塞。長居すると被害が膨らむ。"),
            new Village("cathedral", "聖鎖大聖堂村", 1450, 53, 72, 16, 0, 86400, "祈りで統率された守備隊が粘る後半の壁。総力戦でも楽ではない。"),
            new Village("imperial", "王都外縁の宿場村", 1850, 63, 88, 22, 0, 129600, "王都防衛網の一角。重装兵と魔導砲で長時間出撃を潰しにくる。"),
            new Village("abyss99", "奈落封鎖戦線・99階層", 24000, 92, 138, 50, 99, 1814400, "最終村にして実質ダンジョン。全 99 階を踏破するまでに膨大な時間と戦力を要求する。")
    };

    static final TimeOption[] TIME_OPTIONS = new TimeOption[] {
            new TimeOption("15m", "15分", 15 * 60, "短時間の実地侵攻。被害を抑えやすく、様子見に向く。"),
            new TimeOption("30m", "30分", 30 * 60, "標準的な実時間出撃。戦果と安全のバランスが良い。"),
            new TimeOption("45m", "45分", 45 * 60, "やや長めの侵攻。ここまでは比較的安定して運用しやすい。"),
            new TimeOption("1h", "1時間", 60 * 60, "本格侵攻。戦果は伸びるが、被害も目立ち始める。"),
            new TimeOption("2h", "2時間", 2 * 60 * 60, "長時間遠征。硬い村を削れる反面、全滅の危険が高い。"),
            new TimeOption("3h", "3時間", 3 * 60 * 60, "最長出撃。無理押しすると壊滅しやすい捨て身の長期戦。")
    };

    private GameData() {
    }

    static Monster getMonster(String id) {
        for (Monster monster : MONSTERS) {
            if (monster.id.equals(id)) {
                return monster;
            }
        }
        return MONSTERS[0];
    }

    static Village getVillage(String id) {
        for (Village village : VILLAGES) {
            if (village.id.equals(id)) {
                return village;
            }
        }
        return VILLAGES[0];
    }

    static TimeOption getTimeOption(String id) {
        for (TimeOption timeOption : TIME_OPTIONS) {
            if (timeOption.id.equals(id)) {
                return timeOption;
            }
        }
        return TIME_OPTIONS[0];
    }

    static List<Monster> getMonsters(List<String> ids) {
        List<Monster> monsters = new ArrayList<>();
        for (String id : ids) {
            monsters.add(getMonster(id));
        }
        return monsters;
    }

    static Monster getRecruitRewardMonster(Village village, int milestoneIndex) {
        if (FINAL_DUNGEON_ID.equals(village.id) && milestoneIndex >= RECRUIT_PROGRESS_THRESHOLDS.length - 1) {
            return getMonster("abyssdragon");
        }

        int recruitTier = Math.min(5, Math.max(1, village.requiredDemonLordLevel + (milestoneIndex / 2)));
        List<Monster> candidates = new ArrayList<>();
        for (Monster monster : MONSTERS) {
            if ("abyssdragon".equals(monster.id)) {
                continue;
            }
            if (monster.requiredDemonLordLevel <= recruitTier) {
                candidates.add(monster);
            }
        }
        if (candidates.isEmpty()) {
            return MONSTERS[0];
        }

        int villageIndex = 0;
        for (int i = 0; i < VILLAGES.length; i++) {
            if (VILLAGES[i].id.equals(village.id)) {
                villageIndex = i;
                break;
            }
        }
        int candidateIndex = Math.floorMod((villageIndex * 7) + (milestoneIndex * 3), candidates.size());
        return candidates.get(candidateIndex);
    }

    static int getMonsterExpReward(Village village) {
        return Math.max(30, village.requiredControl / 3);
    }

    static int getDemonLordExpReward(Village village) {
        return Math.max(90, village.requiredControl);
    }

    static boolean isVillageUnlocked(Village village, int demonLordLevel) {
        return demonLordLevel >= village.requiredDemonLordLevel;
    }

    static boolean isMonsterUnlocked(GameState gameState, Monster monster) {
        if (monster.requiredDemonLordLevel > gameState.getDemonLordLevel()) {
            return false;
        }
        if ("abyssdragon".equals(monster.id)) {
            Village finalDungeon = getVillage(FINAL_DUNGEON_ID);
            return gameState.getVillageProgress(finalDungeon.id) >= finalDungeon.requiredControl;
        }
        return true;
    }

    static String getMonsterUnlockRequirement(Monster monster) {
        if ("abyssdragon".equals(monster.id)) {
            return "魔王 Lv" + monster.requiredDemonLordLevel + " / 奈落封鎖戦線・99階層 を制圧";
        }
        return "魔王 Lv" + monster.requiredDemonLordLevel;
    }

    static String getVillageProgressLabel(Village village, int progress) {
        int safeProgress = Math.max(0, Math.min(progress, village.requiredControl));
        int percent = safeProgress * 100 / village.requiredControl;
        if (village.isDungeon()) {
            int clearedFloors = safeProgress * village.dungeonFloors / village.requiredControl;
            return "踏破 " + clearedFloors + " / " + village.dungeonFloors + "階 (" + percent + "%)";
        }
        return "制圧率 " + percent + "%";
    }

    static String getVillageRemainingLabel(Village village, int remainingControl) {
        int safeRemaining = Math.max(0, Math.min(remainingControl, village.requiredControl));
        if (village.isDungeon()) {
            int clearedControl = village.requiredControl - safeRemaining;
            int clearedFloors = clearedControl * village.dungeonFloors / village.requiredControl;
            return "ダンジョン残耐久 " + safeRemaining + " / 踏破 " + clearedFloors + " / " + village.dungeonFloors + "階";
        }
        return "村の残り抵抗値 " + safeRemaining;
    }

    static String getVillageReferenceTimeLabel(Village village) {
        int seconds = Math.max(60, village.referenceClearSeconds);
        int days = seconds / 86400;
        int hours = (seconds % 86400) / 3600;
        int minutes = (seconds % 3600) / 60;
        if (days > 0) {
            return days + "日" + (hours > 0 ? " " + hours + "時間" : "");
        }
        if (hours > 0) {
            return hours + "時間" + (minutes > 0 ? " " + minutes + "分" : "");
        }
        return minutes + "分";
    }

    static SelectionOption[] buildMonsterOptions() {
        SelectionOption[] options = new SelectionOption[MONSTERS.length];
        for (int i = 0; i < MONSTERS.length; i++) {
            Monster monster = MONSTERS[i];
            options[i] = new SelectionOption(
                    monster.id,
                    monster.name + "  解放条件 " + getMonsterUnlockRequirement(monster)
                            + " / ATK " + monster.attack + " / DEF " + monster.defense + " / HP " + monster.maxHp,
                    monster.tagline + "\n" + monster.description
            );
        }
        return options;
    }

    static SelectionOption[] buildVillageOptions(int demonLordLevel) {
        SelectionOption[] options = new SelectionOption[VILLAGES.length];
        for (int i = 0; i < VILLAGES.length; i++) {
            Village village = VILLAGES[i];
            boolean unlocked = isVillageUnlocked(village, demonLordLevel);
            String prefix = unlocked ? "" : "未解放  ";
            String areaType = village.isDungeon() ? "99階ダンジョン" : "村";
            options[i] = new SelectionOption(
                    village.id,
                    prefix + village.name + "  " + areaType
                            + "  解放Lv " + village.requiredDemonLordLevel
                            + " / 必要制圧 " + village.requiredControl,
                    "反撃 " + village.attack + " / 防衛 " + village.defense
                            + "\n攻略目安: " + getVillageReferenceTimeLabel(village)
                            + "\n解放条件: 魔王 Lv" + village.requiredDemonLordLevel
                            + "\n" + village.description,
                    unlocked
            );
        }
        return options;
    }

    static SelectionOption[] buildTimeOptions() {
        SelectionOption[] options = new SelectionOption[TIME_OPTIONS.length];
        for (int i = 0; i < TIME_OPTIONS.length; i++) {
            TimeOption timeOption = TIME_OPTIONS[i];
            options[i] = new SelectionOption(
                    timeOption.id,
                    timeOption.label + " 作戦",
                    timeOption.description
            );
        }
        return options;
    }
}
