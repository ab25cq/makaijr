package com.makaijr;

public final class TimeSelectActivity extends BaseSelectionActivity {
    @Override
    protected String getScreenTitle() {
        return GameText.text("Choose Mission Duration", "作戦時間を選択");
    }

    @Override
    protected String getScreenSubtitle() {
        return GameText.text(
                "Missions progress in real time, even while the app is closed.",
                "出撃は実時間で進む。司令部を閉じていても経過時間ぶん自動で侵攻する。"
        );
    }

    @Override
    protected SelectionOption[] getOptions() {
        return GameData.buildTimeOptions();
    }

    @Override
    protected String getCurrentSelectionKey() {
        return new GameState(this).getSelectedTimeId();
    }

    @Override
    protected void saveSelection(String key) {
        new GameState(this).setSelectedTimeId(key);
    }
}
