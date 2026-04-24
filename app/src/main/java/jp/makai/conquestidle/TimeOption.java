package com.makaijr;

final class TimeOption {
    final String id;
    final String label;
    final int durationSeconds;
    final String description;

    TimeOption(String id, String label, int durationSeconds, String description) {
        this.id = id;
        this.label = label;
        this.durationSeconds = durationSeconds;
        this.description = description;
    }
}
