package org.example.kpitelegrambot.DAO.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class PrinterStatistic {
    private final String date;
    private final String prints_num;
    private final String defects_num;

}
