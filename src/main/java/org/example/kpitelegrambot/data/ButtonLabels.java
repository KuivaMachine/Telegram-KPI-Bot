package org.example.kpitelegrambot.data;

public enum ButtonLabels {
    I_AM_PRINTER("Я печатник \uD83D\uDE0E", "i_am_printer"),
    I_AM_PACKER("Я сборщик \uD83E\uDD29", "i_am_packer"),
    TODAY("today", "today"),
    YESTERDAY("yesterday", "yesterday"),
    DAY_BEFORE_YESTERDAY("day_before_yesterday", "day_before_yesterday");

    private final String label;
    private final String callback;
    public String getLabel() {
        return label;
    }
    public String getCallback() {
        return callback;
    }

    ButtonLabels(String label, String callback) {
        this.label = label;
        this.callback = callback;
    }
}
