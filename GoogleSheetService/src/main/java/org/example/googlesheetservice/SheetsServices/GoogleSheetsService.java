package org.example.googlesheetservice.SheetsServices;


import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.log4j.Log4j2;
import org.example.googlesheetservice.Data.Months;
import org.example.googlesheetservice.Data.PrinterStatistic;
import org.example.googlesheetservice.Data.RowColumn;
import org.example.postgresql.DAO.PostgreSQLController;
import org.example.postgresql.entity.Employee;
import org.example.postgresql.service.EmployeeService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Component
public class GoogleSheetsService {

    private final int marketsNumber = 7;
    private int numberOfDayPrinters;
    private int numberOfNightPrinters;

    private final EmployeeService employeeService;
    private final HashMap<Integer, String> labelNumsList = new HashMap<>();
    public final Sheets sheetService;

    private final String SPREADSHEET_ID = "1NpExJ1FOSxgpkPRFmR5q0lKCeIfD0vujReHkwynY_tY";
    private final int SHEET_ID = 1962952644;
    private final PostgreSQLController postgres;

    public GoogleSheetsService(Sheets sheetService, EmployeeService employeeService, PostgreSQLController postgres) {
        this.sheetService = sheetService;
        this.employeeService = employeeService;
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
            e.printStackTrace();
        }
        return -1;
    }

    public void updateData(String range, ValueRange data) {
        try {
            sheetService.spreadsheets().values().update(SPREADSHEET_ID, range, data)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendData(String range, ValueRange data) {
        try {
            sheetService.spreadsheets().values().append(SPREADSHEET_ID, range, data)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(true)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void setRowsOrColumnWidth(Sheets sheetService, RowColumn object, int width, int start, int end) {
        DimensionProperties properties = new DimensionProperties()
                .setPixelSize(width);

        UpdateDimensionPropertiesRequest updateRowProperties = new UpdateDimensionPropertiesRequest()
                .setRange(new DimensionRange()
                        .setSheetId(SHEET_ID)
                        .setDimension(object.getDescription())
                        .setStartIndex(start)
                        .setEndIndex(end))
                .setProperties(properties)
                .setFields("pixelSize");


        Request changeWithRow = new Request().setUpdateDimensionProperties(updateRowProperties);
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateRequest.setRequests(List.of(changeWithRow));
        try {
            sheetService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void addNewSheet(Sheets sheetService, String title) {
        AddSheetRequest addSheetViewRequest = new AddSheetRequest()
                .setProperties(new SheetProperties()
                        .setTitle(title)
                        .setGridProperties(new GridProperties()
                                .setColumnCount(40)));
        Request addSheet = new Request().setAddSheet(addSheetViewRequest);
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateRequest.setRequests(List.of(addSheet));
        try {
            sheetService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String createNewTable(Sheets sheetService, String title) {

        Spreadsheet newTable = new Spreadsheet()
                .setProperties(new SpreadsheetProperties().setTitle(title));
        try {
            return sheetService.spreadsheets().create(newTable).execute().getSpreadsheetId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void cleanTable(Sheets sheetService) {

        ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
        try {
            sheetService.spreadsheets().values()
                    .clear(SPREADSHEET_ID, "A:AZ", clearValuesRequest)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Очистка всех форматов
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        Request clearFormatsRequest = new Request()
                .setRepeatCell(new RepeatCellRequest()
                        .setRange(new GridRange()
                                .setSheetId(SHEET_ID)
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

    public void mergeCells(Sheets sheetService, GridRange range) {

        MergeCellsRequest mergeCellsRequest = new MergeCellsRequest()
                .setRange(range)
                .setMergeType("MERGE_ALL");
        Request request = new Request().setMergeCells(mergeCellsRequest);
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateRequest.setRequests(List.of(request));
        try {
            sheetService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void setCellStyle(Sheets sheetService, GridRange range, Color color, String textAlignment, int fontSize, boolean setBold, int borderWidth) {
        Border border = new Border()
                .setStyle("SOLID")
                .setWidth(borderWidth);

        Borders borders = new Borders()
                .setTop(border)
                .setBottom(border)
                .setLeft(border)
                .setRight(border);

        TextFormat textFormat = new TextFormat()
                .setFontSize(fontSize)
                .setBold(setBold);

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

        Request request = new Request().setRepeatCell(repeatCellRequest);
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateRequest.setRequests(List.of(request));

        try {
            sheetService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCellBordersStyle(Sheets sheetService, GridRange range, int borderWidth) {
        Border border = new Border()
                .setStyle("SOLID")
                .setWidth(borderWidth);
        UpdateBordersRequest updateBordersRequest = new UpdateBordersRequest()
                .setRange(range)
                .setBottom(border)
                .setTop(border)
                .setLeft(border)
                .setRight(border);

        Request request = new Request()
                .setUpdateBorders(updateBordersRequest);

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(request));

        try {
            sheetService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public void createNewSheet() {

        //ОЧИСТКА ВСЕХ ЯЧЕЕК
        cleanTable(sheetService);


        //СЛИЯНИЕ СТОЛБЦОВ ДЛЯ ШАПКИ
        mergeCells(sheetService, new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(1)
                .setEndRowIndex(2)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35));

        //ШИРИНА ТИТУЛЬНИКА 40
        setRowsOrColumnWidth(sheetService, RowColumn.ROWS, 40, 1, 2);
        //ШИРИНА ПЕРВОГО СТОЛБЦА 340
        setRowsOrColumnWidth(sheetService, RowColumn.COLUMNS, 340, 1, 2);
        //ШИРИНА 2-3 СТОЛБЦА 80
        setRowsOrColumnWidth(sheetService, RowColumn.COLUMNS, 80, 2, 4);
        //ШИРИНА ОСТАЛЬНЫХ СТОЛБЦОВ 50
        setRowsOrColumnWidth(sheetService, RowColumn.COLUMNS, 50, 4, 35);

        //ФОРМАТ ВСЕЙ ТАБЛИЦЫ
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(1)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#ffffff"), "CENTER", 11, false, 1);

        //ФОРМАТ ШАПКИ
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(1)
                .setEndRowIndex(2)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#fffede"), "CENTER", 22, true, 2);
        updateData("B2", new ValueRange().setValues(List.of(List.of(getHeaderTitle()))));

        //ФОРМАТ ТЕКСТА ОСНОВНОГО БЛОКА С ФИО
        setCellStyle(sheetService, new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(12 + numberOfDayPrinters + numberOfNightPrinters + marketsNumber)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), getColorByHEX("#ffffff"), "LEFT", 11, true, 1);

        //ФОРМАТ 2 СТРОКИ С ЧИСЛАМИ
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(3)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#ffffff"), "CENTER", 11, true, 1);
        updateData("C3:AI3", new ValueRange().setValues(List.of(List.of("Общее", "Среднее", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"))));

        //ФОРМАТ СТРОКИ "СБОРКА"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(3)
                .setEndRowIndex(4)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#ffff00"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ СБОРКА"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(4 + marketsNumber)
                .setEndRowIndex(5 + marketsNumber)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#ffff00"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ДНЕВНАЯ СМЕНА  (Напечатано/Брак)"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(6 + marketsNumber)
                .setEndRowIndex(7 + marketsNumber)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#76d2a1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ ДЕНЬ"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(7 + marketsNumber + numberOfDayPrinters)
                .setEndRowIndex(8 + marketsNumber + numberOfDayPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#76d2a1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "НОЧНАЯ СМЕНА"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(9 + marketsNumber + numberOfDayPrinters)
                .setEndRowIndex(10 + marketsNumber + numberOfDayPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#4dd0e1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ НОЧЬ"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(10 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setEndRowIndex(11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#4dd0e1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ ПЕЧАТЬ"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(12 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#d8ffe1"), "LEFT", 11, true, 1);

        //ОСНОВНЫЕ ТЕСТОВЫЕ ПОЛЯ
        updateData("B4", new ValueRange().setValues(List.of(List.of("СБОРКА"))));
        updateData(String.format("B%d", 5 + marketsNumber), new ValueRange().setValues(List.of(List.of("ОБЩЕЕ СБОРКА"))));
        updateData(String.format("B%d", 7 + marketsNumber), new ValueRange().setValues(List.of(List.of("ДНЕВНАЯ СМЕНА  (Напечатано/Брак)"))));
        updateData(String.format("B%d:B%d", 8 + marketsNumber + numberOfDayPrinters, 10 + marketsNumber + numberOfDayPrinters), new ValueRange().setValues(List.of(List.of("ОБЩЕЕ ДЕНЬ"), List.of(""), List.of("НОЧНАЯ СМЕНА"))));
        updateData(String.format("B%d:B%d", 11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters, 13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters), new ValueRange().setValues(List.of(List.of("ОБЩЕЕ НОЧЬ"), List.of(""), List.of("ОБЩЕЕ ПЕЧАТЬ"))));

        //ФОРМАТ ОСНОВНОГО БЛОКА С ФИО И ПОКАЗАТЕЛЯМИ
        setCellBordersStyle(sheetService, new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), 2);

        //ФОРМАТ КОЛОНКИ "ОБЩЕЕ"
        setCellBordersStyle(sheetService, new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(2)
                .setEndColumnIndex(3), 2);

        //ФОРМАТ КОЛОНКИ "СРЕДНЕЕ"
        setCellBordersStyle(sheetService, new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(3)
                .setEndColumnIndex(4), 2);

        //ПОЛЯ МАРКЕТОВ
        List<List<Object>> markets = new ArrayList<>();
        for (int i = 5; i < 5 + marketsNumber; i++) {
            if (labelNumsList.get(i) != null) {
                markets.add(List.of(labelNumsList.get(i)));
            }
        }
        updateData((String.format("B5:B%d", 4 + marketsNumber)), new ValueRange().setValues(markets));

        //ПОЛЯ ДНЕВНЫХ ПЕЧАТНИКОВ
        List<List<Object>> dayPrinters = new ArrayList<>();
        for (int i = 8 + marketsNumber; i < 8 + marketsNumber + numberOfDayPrinters; i++) {
            if (labelNumsList.get(i) != null) {
                dayPrinters.add(List.of(labelNumsList.get(i)));
            }
        }
        updateData((String.format("B%d:B%d", 8 + marketsNumber, 7 + marketsNumber + numberOfDayPrinters)), new ValueRange().setValues(dayPrinters));

        //ПОЛЯ НОЧНЫХ ПЕЧАТНИКОВ
        List<List<Object>> nightPrinters = new ArrayList<>();
        for (int i = 11 + marketsNumber + numberOfDayPrinters; i < 11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters; i++) {
            if (labelNumsList.get(i) != null) {
                nightPrinters.add(List.of(labelNumsList.get(i)));
            }
        }
        updateData((String.format("B%d:B%d", 11 + marketsNumber + numberOfDayPrinters, 10 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)), new ValueRange().setValues(nightPrinters));

    }


    public String getHeaderTitle() {
        LocalDateTime date = LocalDateTime.now();
        String month = Months.valueOf(date.getMonth().toString()).getTranslation();
        return String.format("Статистика KPI за %s %d", month, date.getYear());
    }

    public Color getColorByHEX(String hex) {
        int red = Integer.parseInt(hex.substring(1, 3), 16);
        int green = Integer.parseInt(hex.substring(3, 5), 16);
        int blue = Integer.parseInt(hex.substring(5, 7), 16);
        return new Color().setRed(red / 255f).setGreen(green / 255f).setBlue(blue / 255f);
    }

    public void addPrinterStatistic(PrinterStatistic statistic) {
        int rowNum = 0;
        updateLabelList();
        for (Map.Entry<Integer, String> entry : labelNumsList.entrySet()) {
            if (entry.getValue().equals(statistic.getFio())) {
                rowNum = entry.getKey();
                break;
            }
        }
        if (rowNum != 0) {
            updateData(String.format("%s%d", getColumnLetter(statistic.getDate()), rowNum), new ValueRange().setValues(List.of(List.of(String.format("%s/%s", statistic.getPrints_num(), statistic.getDefects_num())))));
        } else {
            log.error("Строка с пользователем не найдена в листе {}", labelNumsList);
        }

    }

    private String getColumnLetter(String date) {
        String day = date.substring(8, 10);
        return switch (Integer.parseInt(day)) {
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
            default -> throw new IllegalStateException("Дата не была распознана: " + day);
        };
    }


    public void fullUpdateTable() {
        List<Employee> dayPrintersList = employeeService.getListOfDayPrinters();
        List<Employee> nightPrintersList = employeeService.getListOfNightPrinters();
        updateLabelList();
        updatePrinterStatistic(dayPrintersList, nightPrintersList);
        createNewSheet();
    }

    private void updatePrinterStatistic(List<Employee> dayPrintersList, List<Employee> nightPrintersList) {
        for (Employee employee : dayPrintersList) {
           List<org.example.postgresql.entity.PrinterStatistic> dayList = postgres.getAllPrinterStatistic(employee.getChatId());
        }
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
                    labelNumsList.put(i, "WB MHC");
                    break;
                case 6:
                    labelNumsList.put(i, "WB Signum");
                    break;
                case 7:
                    labelNumsList.put(i, "WB Silicosha");
                    break;
                case 8:
                    labelNumsList.put(i, "OZON MHC");
                    break;
                case 9:
                    labelNumsList.put(i, "Yandex MHC");
                    break;
                case 10:
                    labelNumsList.put(i, "WB PrintKid");
                    break;
                case 11:
                    labelNumsList.put(i, "FBO");
                    break;
            }
        }

        for (Employee employee : dayPrintersList) {
            labelNumsList.put(dayKey, employee.getFio());
            dayKey++;
        }

        for (Employee employee : nightPrintersList) {
            labelNumsList.put(nightKey, employee.getFio());
            nightKey++;
        }
    }
}
