package org.example.googlesheetservice;

public enum Months {

    JANUARY("январь"), FEBRUARY("февраль"), MARCH("март");

    private final String translation;

    Months(String translation) {
        this.translation = translation;
    }
    public String getTranslation() {
        return translation;
    }
}
