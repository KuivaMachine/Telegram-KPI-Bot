package org.example.kpitelegrambot.googlesheets;

import lombok.Getter;

@Getter
public enum Months {
    JANUARY ("январь");
    private final String month;
    Months(String month) {
        this.month = month;
    }
}
