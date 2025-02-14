package org.example.googlesheetservice;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.googlesheetservice.SheetsServices.GoogleSheetsService;
import org.example.googlesheetservice.postgresql.entity.PackerStatistic;
import org.example.googlesheetservice.postgresql.entity.PrinterStatistic;
import org.example.googlesheetservice.postgresql.entity.SheetId;
import org.example.googlesheetservice.postgresql.service.DateService;
import org.example.googlesheetservice.postgresql.service.SheetIdService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Log4j2
@RequiredArgsConstructor
@Component
public class StatisticHandler {


    private final SheetIdService sheetIdService;
    private final DateService dateService;
    private final GoogleSheetsService googleSheetsService;

    public void processPrinterStatistic(PrinterStatistic printerStatistic) {
        String sheetName = googleSheetsService.getHeaderTitle(dateService.parseStringToLocalDate(printerStatistic.getDate(), "yyyy-MM-dd"));
        SheetId sheetId = sheetIdService.findByTitle(sheetName);
        log.info(String.format("ОБРАБАТЫВАЕТСЯ ЗАПРОС НА ВСТАВКУ СТАТИСТИКИ ПЕЧАТНИКА %s в таблицу '%s'", printerStatistic.getFio(), sheetName));
        if (sheetId != null) {
            googleSheetsService.addPrinterStatistic(sheetId, printerStatistic);
        } else {
            log.info(String.format("ID ТАБЛИЦЫ '%s' НЕ БЫЛО НАЙДЕНО, ОБРАБАТЫВАЕТСЯ ЗАПРОС НА СОЗДАНИЕ И ПОЛНОЕ ОБНОВЛЕНИЕ ТАБЛИЦЫ", sheetName));
            sheetId = googleSheetsService.createNewSheet(sheetName);
            googleSheetsService.fullUpdateTable(sheetId);
        }
    }

    public void processPackerStatistic(PackerStatistic statistic) {
        String sheetName = googleSheetsService.getHeaderTitle(dateService.parseStringToLocalDate(statistic.getDate(), "yyyy-MM-dd"));
        SheetId sheetId = sheetIdService.findByTitle(sheetName);
        log.info(String.format("ОБРАБАТЫВАЕТСЯ ЗАПРОС НА ВСТАВКУ СТАТИСТИКИ СБОРЩИКА В ТАБЛИЦУ '%s' В ДАТУ %s", sheetName, statistic.getDate()));
        if (sheetId != null) {
            googleSheetsService.addPackerStatistic(sheetId, statistic);
        } else {
            log.info(String.format("ID ТАБЛИЦЫ '%s' НЕ БЫЛО НАЙДЕНО, ОБРАБАТЫВАЕТСЯ ЗАПРОС НА СОЗДАНИЕ И ПОЛНОЕ ОБНОВЛЕНИЕ ТАБЛИЦЫ", sheetName));
            sheetId = googleSheetsService.createNewSheet(sheetName);
            googleSheetsService.fullUpdateTable(sheetId);
        }
    }

    public void processUpdateTable() {
        String sheetName = googleSheetsService.getHeaderTitle(LocalDate.now());
        SheetId sheetId = sheetIdService.findByTitle(sheetName);
        if (sheetId != null) {
            googleSheetsService.fullUpdateTable(sheetId);
        } else {
            log.info(String.format("ID ТАБЛИЦЫ '%s' НЕ БЫЛО НАЙДЕНО, ОБРАБАТЫВАЕТСЯ ЗАПРОС НА СОЗДАНИЕ И ПОЛНОЕ ОБНОВЛЕНИЕ ТАБЛИЦЫ", sheetName));
            sheetId = googleSheetsService.createNewSheet(sheetName);
            googleSheetsService.fullUpdateTable(sheetId);
        }
    }
}
