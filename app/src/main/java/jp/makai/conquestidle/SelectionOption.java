package com.makaijr;

final class SelectionOption {
    final String key;
    final String title;
    final String description;
    final boolean enabled;

    SelectionOption(String key, String title, String description) {
        this(key, title, description, true);
    }

    SelectionOption(String key, String title, String description, boolean enabled) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.enabled = enabled;
    }
}
