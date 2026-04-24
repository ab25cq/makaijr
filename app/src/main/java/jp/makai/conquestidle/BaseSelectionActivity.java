package com.makaijr;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

abstract class BaseSelectionActivity extends Activity {
    private final Map<Integer, SelectionOption> optionByViewId = new LinkedHashMap<>();

    private TextView descriptionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        TextView titleView = findViewById(R.id.choiceTitle);
        TextView subtitleView = findViewById(R.id.choiceSubtitle);
        RadioGroup radioGroup = findViewById(R.id.choiceGroup);
        descriptionView = findViewById(R.id.choiceDescription);
        Button saveButton = findViewById(R.id.choiceSaveButton);

        titleView.setText(getScreenTitle());
        subtitleView.setText(getScreenSubtitle());

        SelectionOption[] options = getOptions();
        String currentKey = getCurrentSelectionKey();
        int firstEnabledId = -1;

        for (SelectionOption option : options) {
            RadioButton button = new RadioButton(this);
            int viewId = ViewIdGenerator.next();
            button.setId(viewId);
            button.setText(option.title);
            button.setTextColor(getColor(option.enabled ? android.R.color.white : android.R.color.darker_gray));
            button.setPadding(16, 24, 16, 24);
            button.setEnabled(option.enabled);
            radioGroup.addView(button);
            optionByViewId.put(viewId, option);
            if (option.enabled && firstEnabledId == -1) {
                firstEnabledId = viewId;
            }
            if (option.enabled && option.key.equals(currentKey)) {
                button.setChecked(true);
                descriptionView.setText(option.description);
            }
        }

        if (radioGroup.getCheckedRadioButtonId() == -1 && firstEnabledId != -1) {
            radioGroup.check(firstEnabledId);
            SelectionOption selected = optionByViewId.get(firstEnabledId);
            if (selected != null) {
                descriptionView.setText(selected.description);
            }
        }

        radioGroup.setOnCheckedChangeListener((group, checkedViewId) -> {
            SelectionOption selected = optionByViewId.get(checkedViewId);
            if (selected != null) {
                descriptionView.setText(selected.description);
            }
        });

        saveButton.setOnClickListener(view -> {
            SelectionOption selected = optionByViewId.get(radioGroup.getCheckedRadioButtonId());
            if (selected != null) {
                saveSelection(selected.key);
            }
            finish();
        });
    }

    protected abstract String getScreenTitle();

    protected abstract String getScreenSubtitle();

    protected abstract SelectionOption[] getOptions();

    protected abstract String getCurrentSelectionKey();

    protected abstract void saveSelection(String key);
}
