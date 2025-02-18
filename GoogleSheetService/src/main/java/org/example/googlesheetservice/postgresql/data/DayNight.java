package org.example.googlesheetservice.postgresql.data;

import lombok.Getter;

@Getter
public enum DayNight {
    DAY("День \uD83C\uDF1E", "day"), NIGHT("Ночь \uD83C\uDF19", "night"), UNKNOWN ("UNKNOWN", "UNKNOWN");

    private final String callback;
    private final String label;

    DayNight(String label, String callback) {
        this.callback = callback;
        this.label = label;

    }

}
