package org.example.googlesheetservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.googlesheetservice.SheetsServices.GoogleSheetsService;
import org.example.postgresql.entity.SheetId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Log4j2
@Component
@RequiredArgsConstructor
public class ScheduleManager {


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
