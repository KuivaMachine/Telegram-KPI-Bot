package org.example.googlesheetservice.Data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Months {

    JANUARY("январь", 1),
    FEBRUARY("февраль", 2),
    MARCH("март", 3),
    APRIL("апрель", 4),
    MAY("май", 5),
    JUNE("июнь", 6),
    JULY("июль", 7),
    AUGUST("август", 8),
    SEPTEMBER("сентябрь", 9),
    OCTOBER("октябрь", 10),
    NOVEMBER("ноябрь", 11),
    DECEMBER("декабрь", 12);

    private final String translation;
    private final int number;


}