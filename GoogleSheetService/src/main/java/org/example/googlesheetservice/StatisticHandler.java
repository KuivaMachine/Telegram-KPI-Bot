package org.example.googlesheetservice;


import lombok.RequiredArgsConstructor;
import org.example.googlesheetservice.Data.Months;
import org.example.googlesheetservice.Data.PrinterStatistic;
import org.example.googlesheetservice.SheetsServices.GoogleSheetsService;
import org.example.postgresql.entity.PackerStatistic;
import org.example.postgresql.entity.SheetId;
import org.example.postgresql.service.DateService;
import org.example.postgresql.service.SheetIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@RequiredArgsConstructor
@Component
public class StatisticHandler {

    private static final Logger log = LoggerFactory.getLogger(StatisticHandler.class);
    private final SheetIdService sheetIdService;
    private final DateService dateService;
    private final GoogleSheetsService googleSheetsService;

    public void processPrinterStatistic(PrinterStatistic printerStatistic) {
        var date = dateService.parseStringToLocalDate(printerStatistic.getDate(),"yyyy-MM-dd");
        String month = Months.valueOf(date.getMonth().toString()).getTranslation();
        SheetId sheetId = sheetIdService.findByTitle(String.format("Статистика KPI за %s %d", month, date.getYear()));
        if (sheetId != null) {
            googleSheetsService.addPrinterStatistic(sheetId, printerStatistic);
            log.info("БЫЛА ДОБАВЛЕНА Printer statistic: {}", printerStatistic);
        }else{
            log.info("SheetId = NULL, sheetIdService не нашел его в таблице");
            sheetId = googleSheetsService.createNewSheet();
            googleSheetsService.fullUpdateTable(sheetId);
            googleSheetsService.addPrinterStatistic(sheetId, printerStatistic);
            log.info("СОЗДАЛ НОВЫЙ ЛИСТ И ЗАПОЛНИЛ ЕГО");
        }
    }

    public void processPackerStatistic(PackerStatistic statistic) {
        var date = dateService.parseStringToLocalDate(statistic.getDate_column(),"yyyy-MM-dd");
        String month = Months.valueOf(date.getMonth().toString()).getTranslation();
        SheetId sheetId = sheetIdService.findByTitle(String.format("Статистика KPI за %s %d", month, date.getYear()));
        if (sheetId != null) {
            googleSheetsService.addPackerStatistic(sheetId, statistic);
            log.info("БЫЛА ДОБАВЛЕНА СТАТИСТИКА СБОРЩИКА: {}", statistic);
        }else{
            log.info("SheetId = NULL, sheetIdService не нашел его в таблице");
            sheetId = googleSheetsService.createNewSheet();
            googleSheetsService.fullUpdateTable(sheetId);
            googleSheetsService.addPackerStatistic(sheetId, statistic);
            log.info("СОЗДАЛ НОВЫЙ ЛИСТ И ЗАПОЛНИЛ ЕГО");
        }
    }

    public void processUpdateTable() {
        LocalDate date = LocalDate.now();
        String month = Months.valueOf(date.getMonth().toString()).getTranslation();
        SheetId sheetId = sheetIdService.findByTitle(String.format("Статистика KPI за %s %d", month, date.getYear()));
        if(sheetId != null) {
            log.info("ПОСТУПИЛА КОМАНДА ОБНОВЛЕНИЯ ТАБЛИЦЫ");
            googleSheetsService.fullUpdateTable(sheetId);
            log.info("ТАБЛИЦА ОБНОВЛЕНА");
        }else{
            log.info("SheetId = NULL, sheetIdService не нашел его в таблице");
           sheetId = googleSheetsService.createNewSheet();
           googleSheetsService.fullUpdateTable(sheetId);
            log.info("СОЗДАЛ НОВЫЙ ЛИСТ И ЗАПОЛНИЛ ЕГО");
        }
    }
}
