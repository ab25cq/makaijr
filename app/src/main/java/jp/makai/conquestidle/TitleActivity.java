package com.makaijr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import java.util.List;

public final class TitleActivity extends Activity {
    private static final int REQUEST_EXPORT_SAVE = 1001;
    private static final int REQUEST_IMPORT_SAVE = 1002;
    private static final int REQUEST_POST_NOTIFICATIONS = 1003;

    private GameState gameState;

    private TextView demonLordValueView;
    private TextView monsterSummaryView;
    private TextView villageSummaryView;
    private TextView timeSummaryView;
    private TextView conquestSummaryView;
    private TextView saveStatusView;
    private Button sortieButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        gameState = new GameState(this);
        demonLordValueView = findViewById(R.id.demonLordValue);
        monsterSummaryView = findViewById(R.id.selectedMonsterValue);
        villageSummaryView = findViewById(R.id.selectedVillageValue);
        timeSummaryView = findViewById(R.id.selectedTimeValue);
        conquestSummaryView = findViewById(R.id.conquestSummary);
        saveStatusView = findViewById(R.id.saveStatusValue);
        sortieButton = findViewById(R.id.sortieButton);

        findViewById(R.id.openMonsterButton).setOnClickListener(view ->
                startActivity(new Intent(this, MonsterSelectActivity.class)));
        findViewById(R.id.openVillageButton).setOnClickListener(view ->
                startActivity(new Intent(this, VillageSelectActivity.class)));
        findViewById(R.id.openTimeButton).setOnClickListener(view ->
                startActivity(new Intent(this, TimeSelectActivity.class)));
        sortieButton.setOnClickListener(view ->
                startActivity(new Intent(this, MissionActivity.class)));

        Button exportButton = findViewById(R.id.exportSaveButton);
        Button importButton = findViewById(R.id.importSaveButton);
        Button resetButton = findViewById(R.id.resetSaveButton);

        exportButton.setOnClickListener(view -> openExportDocument());
        importButton.setOnClickListener(view -> openImportDocument());
        resetButton.setOnClickListener(view -> confirmResetAll());

        ensureNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSummaries();
    }

    private void refreshSummaries() {
        MissionEngine.syncActiveMission(gameState, System.currentTimeMillis());

        List<String> monsterIds = gameState.getSelectedMonsterIds();
        Village village = GameData.getVillage(gameState.getSelectedVillageId());
        TimeOption timeOption = GameData.getTimeOption(gameState.getSelectedTimeId());
        ActiveMission activeMission = gameState.getActiveMission();
        String pendingReport = gameState.getPendingMissionReport();

        int demonLordExp = gameState.getDemonLordExp();
        int demonLordLevel = gameState.getDemonLordLevel();
        demonLordValueView.setText("Lv" + demonLordLevel
                + " / EXP " + Progression.getExpIntoCurrentDemonLordLevel(demonLordExp)
                + " / " + Progression.getDemonLordExpForNextLevel(demonLordLevel)
                + " / 編成上限 " + gameState.getAvailablePartySlots() + "体");

        StringBuilder monsterBuilder = new StringBuilder();
        monsterBuilder.append("編成中 ").append(monsterIds.size()).append(" / ").append(gameState.getAvailablePartySlots()).append("体")
                .append("  所持 ").append(gameState.getOwnedMonsters().size()).append("体");
        if (monsterIds.isEmpty()) {
            monsterBuilder.append("\n出撃可能な個体がいない。");
        }
        for (String monsterId : monsterIds) {
            OwnedMonster ownedMonster = gameState.getOwnedMonster(monsterId);
            if (ownedMonster == null) {
                continue;
            }
            Monster monster = ownedMonster.getMonster();
            int monsterExp = ownedMonster.exp;
            int level = ownedMonster.getLevel();
            monsterBuilder.append("\n")
                    .append(ownedMonster.getDisplayName())
                    .append("  Lv")
                    .append(level)
                    .append("  EXP ")
                    .append(Progression.getExpIntoCurrentMonsterLevel(monsterExp))
                    .append(" / ")
                    .append(Progression.getMonsterExpForNextLevel(level));
        }
        monsterSummaryView.setText(monsterBuilder.toString());

        int progress = gameState.getVillageProgress(village.id);
        if (activeMission != null) {
            MissionNotificationReceiver.schedule(this, activeMission);
            Village activeVillage = GameData.getVillage(activeMission.villageId);
            TimeOption activeTime = GameData.getTimeOption(activeMission.timeId);
            int activeProgress = Math.min(activeVillage.requiredControl, activeMission.initialVillageProgress + activeMission.earnedControl);
            int activeRemainingSeconds = Math.max(0, activeTime.durationSeconds - activeMission.elapsedSeconds);
            villageSummaryView.setText(village.name + " / " + GameData.getVillageProgressLabel(village, progress)
                    + "\n出撃中: " + activeVillage.name + " / " + GameData.getVillageProgressLabel(activeVillage, activeProgress));
            timeSummaryView.setText(timeOption.label + " / " + timeOption.description
                    + "\n進行中: 残り " + MissionEngine.formatDuration(activeRemainingSeconds)
                    + " / 経過 " + MissionEngine.formatDuration(activeMission.elapsedSeconds)
                    + "\n終了予定: " + MissionEngine.formatDateTime(activeMission.expectedCompletionAtEpochMillis));
            sortieButton.setText("出撃中の部隊を見る");
        } else {
            villageSummaryView.setText(village.name + " / " + GameData.getVillageProgressLabel(village, progress));
            timeSummaryView.setText(timeOption.label + " / " + timeOption.description);
            sortieButton.setText(pendingReport.isEmpty() ? "出撃" : "前回の戦果を見る");
        }

        StringBuilder builder = new StringBuilder();
        for (Village entry : GameData.VILLAGES) {
            if (!GameData.isVillageUnlocked(entry, demonLordLevel)) {
                builder.append(entry.name)
                        .append("  未解放 (魔王Lv")
                        .append(entry.requiredDemonLordLevel)
                        .append(")");
            } else {
                int value = gameState.getVillageProgress(entry.id);
                builder.append(entry.name)
                        .append("  ")
                        .append(GameData.getVillageProgressLabel(entry, value));
                if (value >= entry.requiredControl) {
                    builder.append("  征服済み / 再攻略可");
                }
            }
            if (entry.isDungeon()) {
                builder.append("  99階ダンジョン");
            }
            builder.append('\n');
        }
        conquestSummaryView.setText(builder.toString().trim());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        Uri uri = data.getData();
        if (requestCode == REQUEST_EXPORT_SAVE) {
            exportSaveFile(uri);
            return;
        }
        if (requestCode == REQUEST_IMPORT_SAVE) {
            importSaveFile(uri);
        }
    }

    private void openExportDocument() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "makaijr-save.json");
        startActivityForResult(intent, REQUEST_EXPORT_SAVE);
    }

    private void openImportDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_IMPORT_SAVE);
    }

    private void exportSaveFile(Uri uri) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                throw new IOException("output stream is null");
            }
            MissionEngine.syncActiveMission(gameState, System.currentTimeMillis());
            outputStream.write(gameState.exportToJson().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            saveStatusView.setText("保存完了: " + uri.getLastPathSegment());
            Toast.makeText(this, "セーブデータを書き出しました。", Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            saveStatusView.setText("保存失敗: " + e.getMessage());
            Toast.makeText(this, "保存に失敗しました。", Toast.LENGTH_SHORT).show();
        }
    }

    private void importSaveFile(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("input stream is null");
            }
            String json = readAll(inputStream);
            gameState.importFromJson(json);
            refreshSummaries();
            saveStatusView.setText("読込完了: " + uri.getLastPathSegment());
            Toast.makeText(this, "セーブデータを読み込みました。", Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            saveStatusView.setText("読込失敗: " + e.getMessage());
            Toast.makeText(this, "読込に失敗しました。", Toast.LENGTH_SHORT).show();
        }
    }

    private String readAll(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) {
            return;
        }
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, REQUEST_POST_NOTIFICATIONS);
    }

    private void confirmResetAll() {
        new AlertDialog.Builder(this)
                .setTitle("すべて初期化")
                .setMessage("魔王レベル、モンスター経験値、村の進行、出撃中データをすべて初期化する。")
                .setPositiveButton("初期化する", (dialog, which) -> resetAllData())
                .setNegativeButton("やめる", null)
                .show();
    }

    private void resetAllData() {
        MissionNotificationReceiver.cancel(this);
        gameState.resetAll();
        saveStatusView.setText("初期化完了");
        refreshSummaries();
        Toast.makeText(this, "すべて初期化しました。", Toast.LENGTH_SHORT).show();
    }
}
