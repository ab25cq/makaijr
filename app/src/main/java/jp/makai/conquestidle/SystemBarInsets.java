package com.makaijr;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

final class SystemBarInsets {
    private SystemBarInsets() {
    }

    static void applyToContent(Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }
        View root = content.getChildAt(0);
        int originalLeft = root.getPaddingLeft();
        int originalTop = root.getPaddingTop();
        int originalRight = root.getPaddingRight();
        int originalBottom = root.getPaddingBottom();
        if (root instanceof ViewGroup) {
            ((ViewGroup) root).setClipToPadding(false);
        }
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                    originalLeft + insets.getSystemWindowInsetLeft(),
                    originalTop + insets.getSystemWindowInsetTop(),
                    originalRight + insets.getSystemWindowInsetRight(),
                    originalBottom + insets.getSystemWindowInsetBottom()
            );
            return insets;
        });
        root.requestApplyInsets();
    }
}
