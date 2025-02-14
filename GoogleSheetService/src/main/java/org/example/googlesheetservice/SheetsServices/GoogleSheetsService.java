package org.example.googlesheetservice.SheetsServices;


import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.log4j.Log4j2;
import org.example.googlesheetservice.Data.RowColumn;
import org.example.googlesheetservice.postgresql.DAO.PostgreSQLController;
import org.example.googlesheetservice.postgresql.data.Months;
import org.example.googlesheetservice.postgresql.entity.Employee;
import org.example.googlesheetservice.postgresql.entity.PackerStatistic;
import org.example.googlesheetservice.postgresql.entity.PrinterStatistic;
import org.example.googlesheetservice.postgresql.entity.SheetId;
import org.example.googlesheetservice.postgresql.service.EmployeeService;
import org.example.googlesheetservice.postgresql.service.SheetIdService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Log4j2
@Component
public class GoogleSheetsService {

    private final int marketsNumber = 7;
    private int numberOfDayPrinters;
    private int numberOfNightPrinters;

    private final EmployeeService employeeService;
    private final HashMap<Integer, String> labelList = new HashMap<>();
    private final Sheets sheetService;
    @Value("${google.spreadsheet_id}")
    private String SPREADSHEET_ID;

    private final SheetIdService sheetIdService;
    private final PostgreSQLController postgres;
    private int numberOfDaysOfMonth;

    public GoogleSheetsService(Sheets sheetService, EmployeeService employeeService, SheetIdService sheetIdService, PostgreSQLController postgres) {
        this.sheetService = sheetService;
        this.employeeService = employeeService;
        this.sheetIdService = sheetIdService;
        this.postgres = postgres;
    }

    private int findSheetIdByTitle(String title) {
        try {
            Spreadsheet spreadsheet = sheetService.spreadsheets().get(SPREADSHEET_ID).execute();
            List<Sheet> sheets = spreadsheet.getSheets();
            for (Sheet sheet : sheets) {
                if (title.equals(sheet.getProperties().getTitle())) {
                    log.info(String.valueOf(sheet.getProperties().getSheetId()));
                    return sheet.getProperties().getSheetId();
                }
            }
        } catch (IOException e) {
            log.info("ОШИБКА В МЕТОДЕ findSheetIdByTitle() {}", e.getMessage());
        }
        return -1;
    }


    private int generateSheetId() {
        Random random = new Random();
        int length = 7 + random.nextInt(5); // 7 + (0..4) = 7..11
        int min = (int) Math.pow(10, length - 1); // Минимальное значение для длины
        int max = (int) Math.pow(10, length) - 1; // Максимальное значение для длины
        return min + random.nextInt(max - min + 1);
    }

    public SheetId createNewSheet(String sheetTitle) {

        //ЕСЛИ ТАБЛИЦА С ТАКИМ НАЗВАНИЕМ УЖЕ ЕСТЬ
        int currentSheetId = findSheetIdByTitle(sheetTitle);
        if (currentSheetId != -1) {
            log.info(String.format("ТАБЛИЦА С НАЗВАНИЕМ '%s' УЖЕ ЕСТЬ. СОХРАНЯЮ ЕЕ ID", sheetTitle));
            sheetIdService.saveSheetId(new SheetId(currentSheetId, sheetTitle));
            return new SheetId(currentSheetId, sheetTitle);
        }

        int sheetId = generateSheetId();
        AddSheetRequest addSheetViewRequest = new AddSheetRequest()
                .setProperties(new SheetProperties()
                        .setTitle(sheetTitle)
                        .setSheetId(sheetId)
                        .setGridProperties(new GridProperties()
                                .setColumnCount(40)));
        Request addSheet = new Request().setAddSheet(addSheetViewRequest);
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateRequest.setRequests(List.of(addSheet));
        try {
            sheetService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute();
            sheetIdService.saveSheetId(new SheetId(sheetId, sheetTitle));
            log.info(String.format("СОЗДАЛ И СОХРАНИЛ НОВУЮ ТАБЛИЦУ '%s', C ID %d", sheetTitle, sheetId));
        } catch (IOException e) {
            log.error(String.format("ОШИБКА ПРИ СОЗДАНИИ ТАБЛИЦЫ С ID %d - %s", sheetId, e.getMessage()));
        }
        return new SheetId(sheetId, sheetTitle);
    }

    private String createNewSpreadSheet(String title) {
        Spreadsheet newTable = new Spreadsheet()
                .setProperties(new SpreadsheetProperties().setTitle(title));
        try {
            return sheetService.spreadsheets().create(newTable).execute().getSpreadsheetId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void cleanTable(SheetId sheetId) {

        ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
        try {
            sheetService.spreadsheets().values()
                    .clear(SPREADSHEET_ID, String.format("%s!A:AZ", sheetId.getTitle()), clearValuesRequest)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Очистка всех форматов
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        Request clearFormatsRequest = new Request()
                .setRepeatCell(new RepeatCellRequest()
                        .setRange(new GridRange()
                                .setSheetId(sheetId.getSheetId())
                                .setStartRowIndex(0)
                                .setEndRowIndex(1000)
                                .setStartColumnIndex(0)
                                .setEndColumnIndex(50))
                        .setCell(new CellData()
                                .setUserEnteredFormat(new CellFormat()))
                        .setFields("userEnteredFormat"));
        batchUpdateRequest.setRequests(Collections.singletonList(clearFormatsRequest));
        try {
            sheetService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void updateTable2(SheetId sheetId) {
        int SHEET_ID = sheetId.getSheetId();
        String title = sheetId.getTitle();
        this.numberOfDaysOfMonth = postgres.getFirstAndLastDayOfMonth(title).getFirst().lengthOfMonth();

        // Очистка таблицы
        cleanTable(sheetId);

        // Создаем список запросов для пакетного обновления
        List<Request> requests = new ArrayList<>();

        // Слияние ячеек для шапки
        requests.add(new Request().setMergeCells(new MergeCellsRequest()
                .setRange(new GridRange()
                        .setSheetId(SHEET_ID)
                        .setStartRowIndex(1)
                        .setEndRowIndex(2)
                        .setStartColumnIndex(1)
                        .setEndColumnIndex(numberOfDaysOfMonth + 4))
                .setMergeType("MERGE_ALL")));

        // Установка ширины строк и столбцов
        requests.add(createDimensionUpdateRequest(SHEET_ID, RowColumn.ROWS, 40, 1, 2));
        requests.add(createDimensionUpdateRequest(SHEET_ID, RowColumn.COLUMNS, 340, 1, 2));
        requests.add(createDimensionUpdateRequest(SHEET_ID, RowColumn.COLUMNS, 90, 2, 4));
        requests.add(createDimensionUpdateRequest(SHEET_ID, RowColumn.COLUMNS, 60, 4, numberOfDaysOfMonth + 4));

        // Форматирование всей таблицы
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(1)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#ffffff"), "CENTER", 12, false, 1));

        // Выравнивание по правому краю в ячейках статистики
        requests.add(new Request().setRepeatCell(new RepeatCellRequest().setFields("*")
                .setRange(new GridRange()
                        .setSheetId(SHEET_ID)
                        .setStartRowIndex(2)
                        .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                        .setStartColumnIndex(2)
                        .setEndColumnIndex(numberOfDaysOfMonth + 4))
                .setCell(new CellData()
                        .setUserEnteredFormat(new CellFormat()
                                .setHorizontalAlignment("RIGHT")
                                .setBorders(new Borders().setTop(new Border().setStyle("SOLID").setWidth(1)).setBottom(new Border().setStyle("SOLID").setWidth(1)).setLeft(new Border().setStyle("SOLID").setWidth(1)).setRight(new Border().setStyle("SOLID").setWidth(1)))))));

        // Форматирование шапки
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(1)
                .setEndRowIndex(2)
                .setStartColumnIndex(1)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#fffede"), "CENTER", 22, true, 2));

        // Форматирование текста основного блока с ФИО
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(12 + numberOfDayPrinters + numberOfNightPrinters + marketsNumber)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), getColorByHEX("#ffffff"), "LEFT", 11, true, 1));

        // Форматирование 2 строки с числами
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(3)
                .setStartColumnIndex(1)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#ffffff"), "CENTER", 11, true, 1));

        // Форматирование строки "СБОРКА"
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(3)
                .setEndRowIndex(4)
                .setStartColumnIndex(1)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#ffff00"), "LEFT", 11, true, 1));

        // Форматирование строки "ОБЩЕЕ СБОРКА"
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(4 + marketsNumber)
                .setEndRowIndex(5 + marketsNumber)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), getColorByHEX("#ffff00"), "LEFT", 11, true, 1));
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(4 + marketsNumber)
                .setEndRowIndex(5 + marketsNumber)
                .setStartColumnIndex(2)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#ffff00"), "RIGHT", 11, true, 1));


        // Форматирование строки "ДНЕВНАЯ СМЕНА  (Напечатано/Брак)"
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(6 + marketsNumber)
                .setEndRowIndex(7 + marketsNumber)
                .setStartColumnIndex(1)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#76d2a1"), "LEFT", 11, true, 1));

        // Форматирование строки "ОБЩЕЕ ДЕНЬ"
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(7 + marketsNumber + numberOfDayPrinters)
                .setEndRowIndex(8 + marketsNumber + numberOfDayPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), getColorByHEX("#76d2a1"), "LEFT", 11, true, 1));
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(7 + marketsNumber + numberOfDayPrinters)
                .setEndRowIndex(8 + marketsNumber + numberOfDayPrinters)
                .setStartColumnIndex(2)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#76d2a1"), "RIGHT", 11, true, 1));


        // Форматирование строки "НОЧНАЯ СМЕНА"
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(9 + marketsNumber + numberOfDayPrinters)
                .setEndRowIndex(10 + marketsNumber + numberOfDayPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#4dd0e1"), "LEFT", 11, true, 1));

        // Форматирование строки "ОБЩЕЕ НОЧЬ"
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(10 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setEndRowIndex(11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), getColorByHEX("#4dd0e1"), "LEFT", 11, true, 1));
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(10 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setEndRowIndex(11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(2)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#4dd0e1"), "RIGHT", 11, true, 1));


        // Форматирование строки "ОБЩЕЕ ПЕЧАТЬ"
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(12 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), getColorByHEX("#d8ffe1"), "LEFT", 11, true, 1));
        requests.add(createCellStyleRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(12 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(2)
                .setEndColumnIndex(numberOfDaysOfMonth + 4), getColorByHEX("#d8ffe1"), "RIGHT", 11, true, 1));

        // Установка границ для основного блока с ФИО и показателями
        requests.add(createCellBordersRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2)));

        // Установка границ для колонки "ОБЩЕЕ"
        requests.add(createCellBordersRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(2)
                .setEndColumnIndex(3)));

        // Установка границ для колонки "СРЕДНЕЕ"
        requests.add(createCellBordersRequest(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(3)
                .setEndColumnIndex(4)));


        //ПАКЕТНЫЙ ЗАПРОС НА ОБНОВЛЕНИЕ ЯЧЕЕК ТАБЛИЦЫ
        try {
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            sheetService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute();
        } catch (IOException e) {
            log.error(String.format("НЕ УДАЛОСЬ ПОСТРОИТЬ ПОЛЯ ТАБЛИЦЫ '%s', ПРИЧИНА: %s", title, e.getMessage()));
        }


        List<ValueRange> data = new ArrayList<>();
        //ТИТУЛЬНИК
        data.add(new ValueRange()
                .setRange(String.format("%s!B2", title))
                .setValues(List.of(List.of(sheetId.getTitle()))));

        //ФОРМАТ 2 СТРОКИ С ЧИСЛАМИ
        data.add(new ValueRange()
                .setRange(String.format("%s!C3:D3", title))
                .setValues(List.of(List.of("Общее", "Среднее"))));
        List<List<Object>> lists = new ArrayList<>();
        List<Object> numsList = new ArrayList<>();
        for (int i = 1; i <= numberOfDaysOfMonth; i++) {
            numsList.add(String.valueOf(i));
        }
        lists.add(numsList);
        data.add(new ValueRange()
                .setRange(String.format("%s!E3:%s3", title, getColumnLetter(numberOfDaysOfMonth)))
                .setValues(lists));


        data.add(new ValueRange()
                .setRange(String.format("%s!B4", title))
                .setValues(List.of(List.of("СБОРКА"))));

        //ПОЛЯ МАРКЕТОВ
        List<List<Object>> markets = new ArrayList<>();
        for (int i = 5; i < 5 + marketsNumber; i++) {
            if (labelList.get(i) != null) {
                markets.add(List.of(labelList.get(i)));
            }
        }
        data.add(new ValueRange()
                .setRange((String.format("%s!B5:B%d", title, 4 + marketsNumber)))
                .setValues(markets));


        //ЯЧЕЙКА "ОБЩЕЕ СБОРКА"
        data.add(new ValueRange()
                .setRange(String.format("%s!B%d", title, 5 + marketsNumber))
                .setValues(List.of(List.of("ОБЩЕЕ СБОРКА"))));

        //ПОЛЯ КОЛОНКИ "ОБЩЕЕ" ДЛЯ МАРКЕТОВ
        List<List<Object>> general = new ArrayList<>();
        for (int i = 5; i < 5 + marketsNumber; i++) {
            if (labelList.get(i) != null) {
                general.add(List.of(String.format("=СУММ(E%d:%s%d)", i, getColumnLetter(numberOfDaysOfMonth), i)));
            }
        }
        general.add(List.of(String.format("=СУММ(E%d:%s%d)", 5 + marketsNumber, getColumnLetter(numberOfDaysOfMonth), 5 + marketsNumber)));
        data.add(new ValueRange()
                .setRange((String.format("%s!C5:C%d", title, 5 + marketsNumber)))
                .setValues(general));

        //ПОЛЯ КОЛОНКИ "СРЕДНЕЕ" ДЛЯ МАРКЕТОВ
        List<List<Object>> averageMarkets = new ArrayList<>();
        for (int i = 5; i < 5 + marketsNumber; i++) {
            if (labelList.get(i) != null) {
                averageMarkets.add(List.of(String.format("=ЕСЛИОШИБКА(ОКРУГЛ(СРЗНАЧ(E%d:%s%d);0);0)", i, getColumnLetter(numberOfDaysOfMonth), i)));
            }
        }
        averageMarkets.add(List.of(String.format("=ЕСЛИОШИБКА(ОКРУГЛ(СРЗНАЧ(E%d:%s%d);0);0)", 5 + marketsNumber, getColumnLetter(numberOfDaysOfMonth), 5 + marketsNumber)));
        data.add(new ValueRange()
                .setRange((String.format("%s!D5:D%d", title, 5 + marketsNumber)))
                .setValues(averageMarkets));

        //ЯЧЕЙКА "ДНЕВНАЯ СМЕНА  (Напечатано/Брак)"
        data.add(new ValueRange()
                .setRange(String.format("%s!B%d", title, 7 + marketsNumber))
                .setValues(List.of(List.of("ДНЕВНАЯ СМЕНА  (Напечатано/Брак)"))));

        //ПОЛЯ ДНЕВНЫХ ПЕЧАТНИКОВ
        List<List<Object>> dayPrinters = new ArrayList<>();
        for (int i = 8 + marketsNumber; i < 8 + marketsNumber + numberOfDayPrinters; i++) {
            if (labelList.get(i) != null) {
                dayPrinters.add(List.of(labelList.get(i)));
            }
        }
        data.add(new ValueRange()
                .setRange(String.format("%s!B%d:B%d", title, 8 + marketsNumber, 7 + marketsNumber + numberOfDayPrinters))
                .setValues(dayPrinters));

        //ОБЩЕЕ ДЕНЬ
        data.add(new ValueRange()
                .setRange(String.format("%s!B%d:B%d", title, 8 + marketsNumber + numberOfDayPrinters, 10 + marketsNumber + numberOfDayPrinters))
                .setValues(List.of(List.of("ОБЩЕЕ ДЕНЬ"), List.of(""), List.of("НОЧНАЯ СМЕНА"))));

        //ПОЛЯ КОЛОНКИ "ОБЩЕЕ" ДЛЯ ДНЕВНЫХ ПЕЧАТНИКОВ
        List<List<Object>> generalDayPrinters = new ArrayList<>();
        for (int i = 8 + marketsNumber; i < 8 + marketsNumber + numberOfDayPrinters; i++) {
            if (labelList.get(i) != null) {
                String rangeGeneral = String.format("E%d:%s%d", i, getColumnLetter(numberOfDaysOfMonth), i);
                generalDayPrinters.add(List.of(String.format("=СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0)))&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s;\"\\/(\\d+)\"));0))))", rangeGeneral, rangeGeneral)));
            }
        }
        String rangeGeneral = String.format("E%d:%s%d", 8 + marketsNumber + numberOfDayPrinters, getColumnLetter(numberOfDaysOfMonth), 8 + marketsNumber + numberOfDayPrinters);
        generalDayPrinters.add(List.of(String.format("=СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0)))&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s;\"\\/(\\d+)\"));0))))", rangeGeneral, rangeGeneral)));
        data.add(new ValueRange()
                .setRange(String.format("%s!C%d:C%d", title, 8 + marketsNumber, 8 + marketsNumber + numberOfDayPrinters))
                .setValues(generalDayPrinters));

        //ПОЛЯ КОЛОНКИ "СРЕДНЕЕ" ДЛЯ ДНЕВНЫХ ПЕЧАТНИКОВ
        List<List<Object>> averageDayPrinters = new ArrayList<>();
        for (int i = 8 + marketsNumber; i < 8 + marketsNumber + numberOfDayPrinters; i++) {
            if (labelList.get(i) != null) {
                String rangeDayAverage = String.format("E%d:%s%d", i, getColumnLetter(numberOfDaysOfMonth), i);
                averageDayPrinters.add(List.of(String.format("=ЕСЛИОШИБКА(ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s; \"^(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0)&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s;\"\\/(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0);\"0/0\")", rangeDayAverage, rangeDayAverage, rangeDayAverage, rangeDayAverage)));
            }
        }
        String rangeDayAverage = String.format("E%d:%s%d", 8 + marketsNumber + numberOfDayPrinters, getColumnLetter(numberOfDaysOfMonth), 8 + marketsNumber + numberOfDayPrinters);
        averageDayPrinters.add(List.of(String.format("=ЕСЛИОШИБКА(ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s; \"^(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0)&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s;\"\\/(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0);\"0/0\")", rangeDayAverage, rangeDayAverage, rangeDayAverage, rangeDayAverage)));
        data.add(new ValueRange()
                .setRange(String.format("%s!D%d:D%d", title, 8 + marketsNumber, 8 + marketsNumber + numberOfDayPrinters))
                .setValues(averageDayPrinters));

        //СТРОКА "ОБЩЕЕ ДЕНЬ"
        List<Object> formulaGeneralDayPrinter = new ArrayList<>();
        for (int i = 1; i <= numberOfDaysOfMonth; i++) {
            String range = String.format("%s%d:%s%d", getColumnLetter(i), 8 + marketsNumber, getColumnLetter(i), 7 + numberOfDayPrinters + marketsNumber);
            formulaGeneralDayPrinter.add(String.format("=ЕСЛИ(СЧЁТЗ(%s)=0; \"\"; СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0)))&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s;\"\\/(\\d+)\"));0)))))", range, range, range));
        }
        data.add(new ValueRange()
                .setRange(String.format("%s!E%d:%s%d", title, 8 + marketsNumber + numberOfDayPrinters, getColumnLetter(numberOfDaysOfMonth), 8 + marketsNumber + numberOfDayPrinters))
                .setValues(List.of(formulaGeneralDayPrinter)));

        //ПОЛЯ НОЧНЫХ ПЕЧАТНИКОВ
        List<List<Object>> nightPrinters = new ArrayList<>();
        for (int i = 11 + marketsNumber + numberOfDayPrinters; i < 11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters; i++) {
            if (labelList.get(i) != null) {
                nightPrinters.add(List.of(labelList.get(i)));
            }
        }
        data.add(new ValueRange()
                .setRange(String.format("%s!B%d:B%d", title, 11 + marketsNumber + numberOfDayPrinters, 10 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters))
                .setValues(nightPrinters));


        //ПОЛЯ КОЛОНКИ "ОБЩЕЕ" ДЛЯ НОЧНЫХ ПЕЧАТНИКОВ
        List<List<Object>> generalNightPrinters = new ArrayList<>();
        for (int i = 11 + marketsNumber+ numberOfDayPrinters; i < 11 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters; i++) {
            if (labelList.get(i) != null) {
                String rangeNightGeneral = String.format("E%d:%s%d", i, getColumnLetter(numberOfDaysOfMonth), i);
                generalNightPrinters.add(List.of(String.format("=СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0)))&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s;\"\\/(\\d+)\"));0))))", rangeNightGeneral, rangeNightGeneral)));
            }
        }
        String rangeNightGeneral = String.format("E%d:%s%d", 11 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters, getColumnLetter(numberOfDaysOfMonth), 11 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters);
        generalNightPrinters.add(List.of(String.format("=СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0)))&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s;\"\\/(\\d+)\"));0))))", rangeNightGeneral, rangeNightGeneral)));
        data.add(new ValueRange()
                .setRange(String.format("%s!C%d:C%d", title,  11 + marketsNumber + numberOfDayPrinters, 11 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters))
                .setValues(generalNightPrinters));

        //ПОЛЯ КОЛОНКИ "СРЕДНЕЕ" ДЛЯ НОЧНЫХ ПЕЧАТНИКОВ
        List<List<Object>> averageNightPrinters = new ArrayList<>();
        for (int i = 11 + marketsNumber+ numberOfDayPrinters; i < 11 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters; i++) {
            if (labelList.get(i) != null) {
                String rangeNightAverage = String.format("E%d:%s%d", i, getColumnLetter(numberOfDaysOfMonth), i);
                averageNightPrinters.add(List.of(String.format("=ЕСЛИОШИБКА(ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s; \"^(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0)&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s;\"\\/(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0);\"0/0\")", rangeNightAverage, rangeNightAverage, rangeNightAverage, rangeNightAverage)));
            }
        }
        String rangeNightAverage = String.format("E%d:%s%d", 11 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters, getColumnLetter(numberOfDaysOfMonth), 11 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters);
        averageNightPrinters.add(List.of(String.format("=ЕСЛИОШИБКА(ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s; \"^(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0)&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s;\"\\/(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0);\"0/0\")", rangeNightAverage, rangeNightAverage, rangeNightAverage, rangeNightAverage)));
        data.add(new ValueRange()
                .setRange(String.format("%s!D%d:D%d", title, 11 + marketsNumber + numberOfDayPrinters, 11 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters ))
                .setValues(averageNightPrinters));

        //СТРОКА "ОБЩЕЕ НОЧЬ"
        List<Object> formulaGeneralNightPrinter = new ArrayList<>();
        for (int i = 1; i <= numberOfDaysOfMonth; i++) {
            String range = String.format("%s%d:%s%d", getColumnLetter(i), 11 + numberOfDayPrinters + marketsNumber, getColumnLetter(i), 10 + numberOfDayPrinters + numberOfNightPrinters + marketsNumber);
            formulaGeneralNightPrinter.add(String.format("=ЕСЛИ(СЧЁТЗ(%s)=0; \"\"; СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0)))&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s;\"\\/(\\d+)\"));0)))))", range, range, range));
        }
        data.add(new ValueRange()
                .setRange(String.format("%s!E%d:%s%d", title, 11 + numberOfNightPrinters+ marketsNumber + numberOfDayPrinters, getColumnLetter(numberOfDaysOfMonth), 8 + marketsNumber +11 + numberOfNightPrinters+ marketsNumber+ numberOfDayPrinters))
                .setValues(List.of(formulaGeneralNightPrinter)));

        //ОБЩЕЕ НОЧЬ
        data.add(new ValueRange()
                .setRange(String.format("%s!B%d:B%d", title, 11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters, 13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters))
                .setValues(List.of(List.of("ОБЩЕЕ НОЧЬ"), List.of(""), List.of("ОБЩЕЕ ПЕЧАТЬ"))));


        //СТРОКА "ОБЩЕЕ ПЕЧАТЬ"
        List<Object> formulaGeneralPrinter = new ArrayList<>();
        for (int i = 1; i <= numberOfDaysOfMonth; i++) {
            String nightCell = String.format("%s%d",getColumnLetter(i), 11 + numberOfNightPrinters+ marketsNumber + numberOfDayPrinters);
            String dayCell = String.format("%s%d",getColumnLetter(i),  8 + marketsNumber + numberOfDayPrinters);
            formulaGeneralPrinter.add(String.format("=ЕСЛИ(СЧЁТЗ(%s;%s)=0; \"\";СУММ(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0); ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0)) & \"/\" & ОКРУГЛ(СУММ(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"\\/(\\d+)\"));0); ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"\\/(\\d+)\"));0));0))",dayCell,nightCell,dayCell,nightCell,dayCell,nightCell));
        }
        data.add(new ValueRange()
                .setRange(String.format("%s!E%d:%s%d", title, 13 + numberOfNightPrinters+ marketsNumber + numberOfDayPrinters, getColumnLetter(numberOfDaysOfMonth), 8 + marketsNumber +13 + numberOfNightPrinters+ marketsNumber+ numberOfDayPrinters))
                .setValues(List.of(formulaGeneralPrinter)));

        //ЯЧЕЙКА "ОБЩЕЕ ПЕЧАТЬ"
        String nightCell = String.format("C%d", 11 + numberOfNightPrinters+ marketsNumber + numberOfDayPrinters);
        String dayCell = String.format("C%d",  8 + marketsNumber + numberOfDayPrinters);
        data.add(new ValueRange()
                .setRange(String.format("%s!C%d", title, 13 + numberOfNightPrinters+ marketsNumber + numberOfDayPrinters))
                .setValues(List.of(List.of(String.format("=ЕСЛИ(СЧЁТЗ(%s;%s)=0; \"\";СУММ(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0); ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0)) & \"/\" & ОКРУГЛ(СУММ(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"\\/(\\d+)\"));0); ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"\\/(\\d+)\"));0));0))",dayCell,nightCell,dayCell,nightCell,dayCell,nightCell)))));

        //ЯЧЕЙКА "СРЕДНЕЕ ПЕЧАТЬ"
        String rangeAverage = String.format("E%d:%s%d", 13 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters, getColumnLetter(numberOfDaysOfMonth), 13 + marketsNumber + numberOfDayPrinters+numberOfNightPrinters);
        data.add(new ValueRange()
                .setRange(String.format("%s!D%d", title, 13 + numberOfNightPrinters+ marketsNumber + numberOfDayPrinters))
                .setValues(List.of(List.of(String.format("=ЕСЛИОШИБКА(ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s; \"^(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0)&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(REGEXEXTRACT(%s;\"\\/(\\d+)\")*1;0)))/ARRAYFORMULA(СУММ(ЕСЛИ(ДЛСТР(СЖПРОБЕЛЫ(%s)) > 0; 1; 0)));0);\"0/0\")", rangeAverage, rangeAverage, rangeAverage, rangeAverage)))));

        //ЗАПРОС НА ПАКЕТНОЕ ОБНОВЛЕНИЕ ДАННЫХ ТАБЛИЦЫ
        batchUpdateValues(data, sheetId);

    }

    private void batchUpdateValues(List<ValueRange> data, SheetId sheetId) {
        try {
            BatchUpdateValuesRequest batchRequest = new BatchUpdateValuesRequest()
                    .setValueInputOption("USER_ENTERED")
                    .setData(data);
            sheetService.spreadsheets().values()
                    .batchUpdate(SPREADSHEET_ID, batchRequest)
                    .execute();
        } catch (IOException e) {
            log.error(String.format("НЕ УДАЛОСЬ ВЫПОЛНИТЬ ОБНОВЛЕНИЕ ОСНОВНЫХ ТЕКСТОВЫХ ПОЛЕЙ ТАБЛИЦЫ '%s' ПО ПРИЧИНЕ: %s", sheetId.getTitle(), e.getMessage()));
        }
    }

    private Request createDimensionUpdateRequest(int sheetId, RowColumn object, int width, int start, int end) {
        DimensionProperties properties = new DimensionProperties().setPixelSize(width);
        UpdateDimensionPropertiesRequest updateRowProperties = new UpdateDimensionPropertiesRequest()
                .setRange(new DimensionRange()
                        .setSheetId(sheetId)
                        .setDimension(object.getDescription())
                        .setStartIndex(start)
                        .setEndIndex(end))
                .setProperties(properties)
                .setFields("pixelSize");
        return new Request().setUpdateDimensionProperties(updateRowProperties);
    }

    private Request createCellStyleRequest(GridRange range, Color color, String textAlignment, int fontSize, boolean setBold, int borderWidth) {
        Border border = new Border().setStyle("SOLID").setWidth(borderWidth);
        Borders borders = new Borders().setTop(border).setBottom(border).setLeft(border).setRight(border);
        TextFormat textFormat = new TextFormat().setFontSize(fontSize).setBold(setBold);
        CellData cellData = new CellData()
                .setUserEnteredFormat(new CellFormat()
                        .setHorizontalAlignment(textAlignment)
                        .setBackgroundColor(color)
                        .setTextFormat(textFormat)
                        .setBorders(borders));
        RepeatCellRequest repeatCellRequest = new RepeatCellRequest()
                .setRange(range)
                .setFields("*")
                .setCell(cellData);
        return new Request().setRepeatCell(repeatCellRequest);
    }

    private Request createCellBordersRequest(GridRange range) {
        Border border = new Border().setStyle("SOLID").setWidth(2);
        UpdateBordersRequest updateBordersRequest = new UpdateBordersRequest()
                .setRange(range)
                .setBottom(border)
                .setTop(border)
                .setLeft(border)
                .setRight(border);
        return new Request().setUpdateBorders(updateBordersRequest);
    }


    private List<List<Object>> readValues(String range) {
        ValueRange response;
        try {
            response = sheetService.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response.getValues();
    }

    public String getHeaderTitle(LocalDate date) {
        String month = Months.valueOf(date.getMonth().toString()).getTranslation();
        return String.format("Статистика KPI за %s %d", month, date.getYear());
    }

    private Color getColorByHEX(String hex) {
        int red = Integer.parseInt(hex.substring(1, 3), 16);
        int green = Integer.parseInt(hex.substring(3, 5), 16);
        int blue = Integer.parseInt(hex.substring(5, 7), 16);
        return new Color().setRed(red / 255f).setGreen(green / 255f).setBlue(blue / 255f);
    }

    private void updateLabelList() {
        List<Employee> dayPrintersList = employeeService.getListOfDayPrinters();
        List<Employee> nightPrintersList = employeeService.getListOfNightPrinters();
        this.numberOfDayPrinters = dayPrintersList.size();
        this.numberOfNightPrinters = nightPrintersList.size();

        int dayKey = 8 + marketsNumber;
        int nightKey = 11 + marketsNumber + numberOfDayPrinters;
        for (int i = 5; i <= 5 + marketsNumber; i++) {
            switch (i) {
                case 5:
                    labelList.put(i, "WB MHC");
                    break;
                case 6:
                    labelList.put(i, "WB Signum");
                    break;
                case 7:
                    labelList.put(i, "WB Silicosha");
                    break;
                case 8:
                    labelList.put(i, "OZON MHC");
                    break;
                case 9:
                    labelList.put(i, "Yandex MHC");
                    break;
                case 10:
                    labelList.put(i, "WB PrintKid");
                    break;
                case 11:
                    labelList.put(i, "FBO");
                    break;
            }
        }

        for (Employee employee : dayPrintersList) {
            labelList.put(dayKey, employee.getFio());
            dayKey++;
        }

        for (Employee employee : nightPrintersList) {
            labelList.put(nightKey, employee.getFio());
            nightKey++;
        }

    }

    public void addPackerStatistic(SheetId sheetId, PackerStatistic statistic) {
        updateLabelList();
        List<ValueRange> data = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : labelList.entrySet()) {
            ValueRange vr = new ValueRange().setRange(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate()), entry.getKey()));
            if (entry.getValue().equals("WB MHC")) {
                data.add(vr.setValues(List.of(List.of(statistic.getWb_mhc()))));
            }
            if (entry.getValue().equals("WB Signum")) {
                data.add(vr.setValues(List.of(List.of(statistic.getWb_signum()))));
            }
            if (entry.getValue().equals("WB Silicosha")) {
                data.add(vr.setValues(List.of(List.of(statistic.getWb_silicosha()))));
            }
            if (entry.getValue().equals("OZON MHC")) {
                data.add(vr.setValues(List.of(List.of(statistic.getOzon()))));
            }
            if (entry.getValue().equals("Yandex MHC")) {
                data.add(vr.setValues(List.of(List.of(statistic.getYandex()))));
            }
            if (entry.getValue().equals("WB PrintKid")) {
                data.add(vr.setValues(List.of(List.of(statistic.getWb_printkid()))));
            }
            if (entry.getValue().equals("FBO")) {
                data.add(vr.setValues(List.of(List.of(statistic.getFbo()))));
            }
        }
        data.add(new ValueRange()
                .setRange(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate()), 5 + marketsNumber))
                .setValues(List.of(List.of(String.format("=СУММ(%s5:%s%d)", getColumnLetter(statistic.getDate()), getColumnLetter(statistic.getDate()), 4 + marketsNumber)))));

        batchUpdateValues(data, sheetId);
        log.info(String.format("БЫЛА ДОБАВЛЕНА СТАТИСТИКА СБОРЩИКА: %s", statistic));
    }

    public void addPrinterStatistic(SheetId sheetId, PrinterStatistic statistic) {
        updateLabelList();
        boolean isEmployeeInTable = false;
        List<List<Object>> values = readValues(String.format("%s!B1:B%d", sheetId.getTitle(), 20 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters));
        for (List<Object> value : values) {
            if (!value.isEmpty()) {
                if (value.getFirst().equals(statistic.getFio())) {
                    isEmployeeInTable = true;
                    break;
                }
            }
        }
        if (!isEmployeeInTable) {
            log.info(String.format("ПЕЧАТНИКА %s В ТАБЛИЦЕ '%s' НЕТ", statistic.getFio(), sheetId.getTitle()));
            fullUpdateTable(sheetId);
        }

        for (Map.Entry<Integer, String> entry : labelList.entrySet()) {
            if (entry.getValue().equals(statistic.getFio())) {
                try {
                    List<ValueRange> data = new ArrayList<>();
                    data.add(new ValueRange().setRange(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate()), entry.getKey())).setValues(List.of(List.of(String.format("%s/%s", statistic.getPrints_num(), statistic.getDefects_num())))));
                    String range = String.format("%s%d:%s%d", getColumnLetter(statistic.getDate()), 8 + marketsNumber, getColumnLetter(statistic.getDate()), 7 + marketsNumber + numberOfDayPrinters);
                    data.add(new ValueRange().setRange(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate()), 8 + marketsNumber + numberOfDayPrinters)).setValues(List.of(List.of(String.format("=СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s; \"^(\\d+)\"));0)))&\"/\"&ОКРУГЛ(СУММ(ARRAYFORMULA(ЕСЛИОШИБКА(ЗНАЧЕН(REGEXEXTRACT(%s;\"\\/(\\d+)\"));0))))", range, range)))));
                    BatchUpdateValuesRequest batchRequest = new BatchUpdateValuesRequest()
                            .setValueInputOption("USER_ENTERED")
                            .setData(data);
                    sheetService.spreadsheets().values()
                            .batchUpdate(SPREADSHEET_ID, batchRequest)
                            .execute();
                } catch (IOException e) {
                    log.error(String.format("НЕ УДАЛОСЬ ВЫПОЛНИТЬ ОБНОВЛЕНИЕ ОСНОВНЫХ ТЕКСТОВЫХ ПОЛЕЙ ТАБЛИЦЫ '%s' ПО ПРИЧИНЕ: %s", sheetId.getTitle(), e.getMessage()));
                }
                log.info(String.format("БЫЛА ДОБАВЛЕНА СТАТИСТИКА ПЕЧАТНИКА %s - %s", statistic.getFio(), statistic));
                break;
            }
        }
    }

    private String getColumnLetter(Object date) {
        int day = 0;
        if (date instanceof String) {
            day = Integer.parseInt(((String) date).substring(8, 10));
        } else if (date instanceof Integer) {
            day = (int) date;
        }
        return switch (day) {
            case 1 -> "E";
            case 2 -> "F";
            case 3 -> "G";
            case 4 -> "H";
            case 5 -> "I";
            case 6 -> "J";
            case 7 -> "K";
            case 8 -> "L";
            case 9 -> "M";
            case 10 -> "N";
            case 11 -> "O";
            case 12 -> "P";
            case 13 -> "Q";
            case 14 -> "R";
            case 15 -> "S";
            case 16 -> "T";
            case 17 -> "U";
            case 18 -> "V";
            case 19 -> "W";
            case 20 -> "X";
            case 21 -> "Y";
            case 22 -> "Z";
            case 23 -> "AA";
            case 24 -> "AB";
            case 25 -> "AC";
            case 26 -> "AD";
            case 27 -> "AE";
            case 28 -> "AF";
            case 29 -> "AG";
            case 30 -> "AH";
            case 31 -> "AI";
            default ->
                    throw new IllegalStateException("Дата не была распознана в методе getColumnLetter(), дата = : " + day);
        };
    }


    public void fullUpdateTable(SheetId sheetId) {
        log.info(String.format("ВЫПОЛНЯЮ ПОЛНОЕ ОБНОВЛЕНИЕ ТАБЛИЦЫ '%s'", sheetId.getTitle()));
        List<Employee> dayPrintersList = employeeService.getListOfDayPrinters();
        List<Employee> nightPrintersList = employeeService.getListOfNightPrinters();
        List<PackerStatistic> packerStatisticList = postgres.getAllPackerStatistics(sheetId.getTitle());
        updateLabelList();
        updateTable2(sheetId);
        updateAllStatistic(sheetId, dayPrintersList, nightPrintersList, packerStatisticList);
        log.info(String.format("ТАБЛИЦА '%s' ПОЛНОСТЬЮ ОБНОВЛЕНА АКТУАЛЬНЫМИ ДАННЫМИ", sheetId.getTitle()));
    }

    private void updateAllStatistic(SheetId sheetId, List<Employee> dayPrintersList, List<Employee> nightPrintersList, List<PackerStatistic> packerStatisticList) {
        if (dayPrintersList != null) {
            updatePrinterStatistic(sheetId, dayPrintersList);
        }
        if (nightPrintersList != null) {
            updatePrinterStatistic(sheetId, nightPrintersList);
        }
        if (packerStatisticList != null) {
            updatePackerStatistic(sheetId, packerStatisticList);
        }
    }

    private void updatePackerStatistic(SheetId sheetId, List<PackerStatistic> packerStatisticList) {
        for (PackerStatistic packerStatistic : packerStatisticList) {
            addPackerStatistic(sheetId, packerStatistic);
        }
    }

    private void updatePrinterStatistic(SheetId sheetId, List<Employee> printersList) {
        List<ValueRange> data = new ArrayList<>();
        Object[] statistic = new Object[numberOfDaysOfMonth];
        for (Employee employee : printersList) {
            ValueRange vr = new ValueRange();
            Arrays.fill(statistic, "");
            for (Map.Entry<Integer, String> entry : labelList.entrySet()) {
                if (entry.getValue().equals(employee.getFio())) {
                    List<PrinterStatistic> stat_list = postgres.getPrinterStatisticByChatId(employee.getChatId(), sheetId.getTitle());
                    if (stat_list != null) {
                        for (PrinterStatistic ps : stat_list) {
                            statistic[Integer.parseInt(ps.getDate().substring(8, 10)) - 1] = String.format("%s/%s", ps.getPrints_num(), ps.getDefects_num());


                        }
                    }
                    vr.setRange(String.format("%s!E%d:%s%d", sheetId.getTitle(), entry.getKey(), getColumnLetter(numberOfDaysOfMonth), entry.getKey()));
                    vr.setValues(List.of(Arrays.stream(statistic).toList()));
                }
            }
            data.add(vr);
        }
        batchUpdateValues(data, sheetId);
    }
}
