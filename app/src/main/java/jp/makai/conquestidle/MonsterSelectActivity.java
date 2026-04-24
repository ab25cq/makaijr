package com.makaijr;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public final class MonsterSelectActivity extends Activity {
    private final List<CheckBox> checkBoxes = new ArrayList<>();
    private final List<OwnedMonster> displayedMonsters = new ArrayList<>();

    private GameState gameState;
    private TextView statusView;
    private TextView descriptionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monster_select);

        gameState = new GameState(this);

        TextView subtitleView = findViewById(R.id.choiceSubtitle);
        LinearLayout container = findViewById(R.id.monsterListContainer);
        statusView = findViewById(R.id.partyStatusValue);
        descriptionView = findViewById(R.id.choiceDescription);
        Button saveButton = findViewById(R.id.choiceSaveButton);

        subtitleView.setText("所持している個体だけ編成できる。全滅した個体は消滅し、征服率で新たな配下が増える。");

        List<String> selectedIds = gameState.getSelectedMonsterIds();
        List<OwnedMonster> ownedMonsters = gameState.getOwnedMonsters();
        for (OwnedMonster ownedMonster : ownedMonsters) {
            Monster monster = ownedMonster.getMonster();
            int level = ownedMonster.getLevel();
            CheckBox box = new CheckBox(this);
            box.setText(ownedMonster.getDisplayName()
                    + "  現在Lv " + level
                    + "  ATK " + Progression.getMonsterAttack(monster, level)
                    + " / DEF " + Progression.getMonsterDefense(monster, level)
                    + " / HP " + Progression.getMonsterHp(monster, level));
            box.setTextColor(getColor(android.R.color.white));
            box.setPadding(16, 24, 16, 24);
            box.setChecked(selectedIds.contains(ownedMonster.instanceId));
            box.setOnClickListener(view -> {
                if (getCheckedCount() > gameState.getAvailablePartySlots()) {
                    box.setChecked(false);
                    Toast.makeText(
                            this,
                            "今の魔王レベルでは " + gameState.getAvailablePartySlots() + " 体までしか編成できない。",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                updateStatus(ownedMonster);
            });
            container.addView(box);
            checkBoxes.add(box);
            displayedMonsters.add(ownedMonster);
        }

        if (ownedMonsters.isEmpty()) {
            statusView.setText("所持モンスター 0体");
            descriptionView.setText("全滅で戦力が尽きている。村の報酬を得るには、初期化するか既存の生存個体を残す必要がある。");
            saveButton.setEnabled(false);
            return;
        }

        OwnedMonster focused = gameState.getOwnedMonster(selectedIds.isEmpty() ? ownedMonsters.get(0).instanceId : selectedIds.get(0));
        updateStatus(focused == null ? ownedMonsters.get(0) : focused);

        saveButton.setOnClickListener(view -> {
            List<String> newSelection = new ArrayList<>();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    newSelection.add(displayedMonsters.get(i).instanceId);
                }
            }
            if (newSelection.isEmpty()) {
                Toast.makeText(this, "最低 1 体は選んでください。", Toast.LENGTH_SHORT).show();
                return;
            }
            gameState.setSelectedMonsterIds(newSelection);
            finish();
        });
    }

    private int getCheckedCount() {
        int count = 0;
        for (CheckBox box : checkBoxes) {
            if (box.isChecked()) {
                count++;
            }
        }
        return count;
    }

    private void updateStatus(OwnedMonster focusedMonster) {
        int demonLordLevel = gameState.getDemonLordLevel();
        int demonLordExp = gameState.getDemonLordExp();
        int checkedCount = getCheckedCount();
        int partySlots = gameState.getAvailablePartySlots();
        int monsterExp = focusedMonster.exp;
        int monsterLevel = focusedMonster.getLevel();
        Monster monster = focusedMonster.getMonster();

        statusView.setText("魔王 Lv" + demonLordLevel
                + "  EXP " + Progression.getExpIntoCurrentDemonLordLevel(demonLordExp)
                + " / " + Progression.getDemonLordExpForNextLevel(demonLordLevel)
                + "  編成 " + checkedCount + " / " + partySlots
                + "  所持 " + gameState.getOwnedMonsters().size() + "体");

        descriptionView.setText(focusedMonster.getDisplayName()
                + "\n"
                + monster.tagline
                + "\n"
                + monster.description
                + "\n\n現在の成長: Lv" + monsterLevel
                + " / EXP " + Progression.getExpIntoCurrentMonsterLevel(monsterExp)
                + " / " + Progression.getMonsterExpForNextLevel(monsterLevel));
    }
}
