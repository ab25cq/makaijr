package com.makaijr;

public final class VillageSelectActivity extends BaseSelectionActivity {
    @Override
    protected String getScreenTitle() {
        return "侵攻する村を選択";
    }

    @Override
    protected String getScreenSubtitle() {
        return "村ごとに防衛力と解放条件が違う。最終村は 99 階ダンジョン。";
    }

    @Override
    protected SelectionOption[] getOptions() {
        return GameData.buildVillageOptions(new GameState(this).getDemonLordLevel());
    }

    @Override
    protected String getCurrentSelectionKey() {
        return new GameState(this).getSelectedVillageId();
    }

    @Override
    protected void saveSelection(String key) {
        new GameState(this).setSelectedVillageId(key);
    }
}
