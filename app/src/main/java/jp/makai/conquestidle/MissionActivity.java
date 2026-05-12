package com.makaijr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public final class MissionActivity extends Activity {
    private static final long TICK_MS = 1000L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private GameState gameState;

    private TextView missionHeaderView;
    private TextView partyValueView;
    private TextView timeView;
    private TextView villageHpView;
    private TextView monsterHpView;
    private TextView resultView;
    private TextView logView;
    private ProgressBar missionProgressView;
    private Button returnButton;

    private final Runnable refreshTick = new Runnable() {
        @Override
        public void run() {
            refreshMissionState();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);
        SystemBarInsets.applyToContent(this);

        gameState = new GameState(this);

        missionHeaderView = findViewById(R.id.missionHeader);
        partyValueView = findViewById(R.id.partyValue);
        timeView = findViewById(R.id.timeValue);
        villageHpView = findViewById(R.id.villageHpValue);
        monsterHpView = findViewById(R.id.monsterHpValue);
        resultView = findViewById(R.id.missionResult);
        logView = findViewById(R.id.battleLog);
        missionProgressView = findViewById(R.id.missionProgress);
        returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(view -> {
            if (!gameState.getPendingMissionReport().isEmpty()) {
                gameState.consumePendingMissionReport();
            }
            Intent intent = new Intent(this, TitleActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMissionState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshTick);
    }

    private void refreshMissionState() {
        handler.removeCallbacks(refreshTick);
        MissionEngine.syncActiveMission(gameState, System.currentTimeMillis());

        ActiveMission activeMission = gameState.getActiveMission();
        if (activeMission == null && gameState.getPendingMissionReport().isEmpty()) {
            MissionEngine.startMission(gameState, System.currentTimeMillis());
            activeMission = gameState.getActiveMission();
        }

        if (activeMission != null) {
            renderActiveMission(activeMission);
            handler.postDelayed(refreshTick, TICK_MS);
            return;
        }

        String pendingReport = gameState.getPendingMissionReport();
        if (!pendingReport.isEmpty()) {
            renderCompletedMission(pendingReport);
            return;
        }

        renderUnavailableMission();
    }

    private void renderActiveMission(ActiveMission activeMission) {
        Village village = GameData.getVillage(activeMission.villageId);
        TimeOption timeOption = GameData.getTimeOption(activeMission.timeId);

        missionHeaderView.setText(GameText.villageName(village)
                + (village.isDungeon() ? GameText.text(" in progress", " を踏破中") : GameText.text(" assault in progress", " へ侵攻中")));
        partyValueView.setText(buildPartySummary(activeMission.partyMonsterIds));
        missionProgressView.setMax(timeOption.durationSeconds);
        missionProgressView.setProgress(activeMission.elapsedSeconds);

        int totalProgress = Math.min(village.requiredControl, activeMission.initialVillageProgress + activeMission.earnedControl);
        int remainingSeconds = Math.max(0, timeOption.durationSeconds - activeMission.elapsedSeconds);

        timeView.setText(GameText.text("Remaining ", "残り ") + MissionEngine.formatDuration(remainingSeconds)
                + GameText.text(" / elapsed ", " / 経過 ") + MissionEngine.formatDuration(activeMission.elapsedSeconds)
                + GameText.text(" / ends around ", " / 終了予定 ") + MissionEngine.formatDateTime(activeMission.expectedCompletionAtEpochMillis)
                + GameText.text(" / selected ", " / 選択時間 ") + GameText.timeLabel(timeOption));
        villageHpView.setText(GameData.getVillageRemainingLabel(village, activeMission.remainingVillageControl)
                + " / " + GameData.getVillageProgressLabel(village, totalProgress));
        monsterHpView.setText(GameText.text("Party HP ", "部隊HP ") + activeMission.partyHp + " / " + activeMission.partyMaxHp + GameText.text(" / tribute ", " / 供物 ") + activeMission.tribute);
        resultView.setText(GameText.text(
                "Mission active. Progress continues in real time while away from command.",
                "出撃中。司令部へ戻っても、残り時間が経過すれば自動で戦果が反映される。"
        ));
        logView.setText(activeMission.lastLogLine
                + GameText.text("\n\nCurrent gains: conquest +", "\n\n現在の戦果: 制圧進行 +") + activeMission.earnedControl
                + GameText.text("\nProgress: real time / background supported", "\n進行方式: 実時間連動 / バックグラウンド進行対応"));
        returnButton.setEnabled(true);
    }

    private void renderCompletedMission(String pendingReport) {
        missionHeaderView.setText(GameText.text("Last Report", "前回の戦果"));
        partyValueView.setText(GameText.text("The mission has ended. Review it, then return to command.", "出撃は終了している。確認後に司令部へ戻れる。"));
        missionProgressView.setMax(1);
        missionProgressView.setProgress(1);
        timeView.setText(GameText.text("Auto mission complete", "自動遠征は完了"));
        villageHpView.setText(GameText.text("Choose the next target and deploy again.", "次の村を選び、再度出撃できる。"));
        monsterHpView.setText(GameText.text("Rewards have been applied.", "報酬は反映済み。"));
        resultView.setText(pendingReport);
        logView.setText(GameText.text("Reviewing results. Return to command to prepare the next mission.", "結果確認中。司令部へ戻ると次の出撃準備に入れる。"));
        returnButton.setEnabled(true);
    }

    private void renderUnavailableMission() {
        Village village = GameData.getVillage(gameState.getSelectedVillageId());
        missionHeaderView.setText(GameText.text("Mission Setup", "出撃準備"));
        partyValueView.setText(buildPartySummary(gameState.getSelectedMonsterIds()));
        missionProgressView.setMax(1);
        missionProgressView.setProgress(0);
        timeView.setText(GameText.text("Mission cannot start", "出撃を開始できない状態"));
        if (!gameState.hasOwnedMonsters()) {
            villageHpView.setText(GameText.text("No owned units.", "所持モンスターがいない。"));
            resultView.setText(GameText.text("No forces remain. Reset or import another save to recover.", "全滅で部隊が消えた。初期化するか、別のセーブから立て直す必要がある。"));
        } else if (!GameData.isVillageUnlocked(village, gameState.getDemonLordLevel())) {
            villageHpView.setText(GameText.text("Locked: requires Demon Lord Lv", "未解放: 魔王 Lv") + village.requiredDemonLordLevel + GameText.text("", " で挑戦可能"));
            resultView.setText(GameText.text("This target is still locked. Raise Demon Lord level first.", "この村はまだ未解放。先に魔王レベルを上げる必要がある。"));
        } else {
            villageHpView.setText(GameText.text("Opening this screen starts a real-time mission.", "この画面を開くと実時間出撃が始まる。"));
            resultView.setText(GameText.text("Mission could not start. Check settings and try again.", "ミッションを開始できなかった。設定を確認してから再試行する。"));
        }
        monsterHpView.setText(GameText.text("Waiting at command", "司令部待機中"));
        logView.setText(GameText.text("When deployment is possible, this screen starts the mission automatically.", "出撃可能な状態になれば、この画面から自動遠征を開始する。"));
        returnButton.setEnabled(true);
    }

    private String buildPartySummary(List<String> partyMonsterIds) {
        StringBuilder builder = new StringBuilder();
        builder.append(GameText.demonLord()).append(" Lv").append(gameState.getDemonLordLevel()).append(GameText.text(" / Party ", " / 編成 "));
        if (partyMonsterIds.isEmpty()) {
            builder.append(GameText.text("none", "なし"));
            return builder.toString();
        }
        for (int i = 0; i < partyMonsterIds.size(); i++) {
            OwnedMonster ownedMonster = gameState.getOwnedMonster(partyMonsterIds.get(i));
            if (ownedMonster == null) {
                continue;
            }
            if (i > 0) {
                builder.append(" / ");
            }
            builder.append(ownedMonster.getDisplayName())
                    .append(" Lv")
                    .append(ownedMonster.getLevel());
        }
        return builder.toString();
    }
}
