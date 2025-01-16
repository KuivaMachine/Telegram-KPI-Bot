package org.example.kpitelegrambot.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ButtonLabels {
    I_AM_PRINTER("Я печатник \uD83D\uDE0E", "i_am_printer"),
    I_AM_PACKER("Я сборщик \uD83E\uDD29", "i_am_packer"),
    TODAY("today", "today"),
    YESTERDAY("yesterday", "yesterday"),
    DAY_BEFORE_YESTERDAY("day_before_yesterday", "day_before_yesterday"),
    SHOW_STATISTIC("Показать, что я записал", "show_statistic");

    String label;
    String callback;

}
