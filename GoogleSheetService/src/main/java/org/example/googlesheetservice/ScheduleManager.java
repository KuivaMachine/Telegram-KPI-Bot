package org.example.googlesheetservice;

import lombok.RequiredArgsConstructor;
import org.example.googlesheetservice.SheetsServices.GoogleSheetsService;
import org.example.postgresql.entity.SheetId;
import org.example.postgresql.service.SheetIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class ScheduleManager {

    private static final Logger log = LoggerFactory.getLogger(ScheduleManager.class);
    private final GoogleSheetsService googleSheetsService;


    @Scheduled(cron = "0 0 0 1 * ?")
    public void doWork() {
        LocalDate today = LocalDate.now();
        String sheetName = googleSheetsService.getHeaderTitle(today);
        SheetId sheetId = googleSheetsService.createNewSheet(sheetName);
        log.info(String.format("СЕГОДНЯ %s, НАЧИНАЮ СОЗДАНИЕ НОВОЙ ТАБЛИЦЫ '%s'", today, sheetId.getTitle()));
        googleSheetsService.fullUpdateTable(sheetId);
    }

}
