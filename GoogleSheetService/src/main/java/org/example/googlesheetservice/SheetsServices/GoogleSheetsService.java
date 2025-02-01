package org.example.googlesheetservice.SheetsServices;


import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.log4j.Log4j2;
import org.example.googlesheetservice.Data.Months;
import org.example.googlesheetservice.Data.PrinterStatistic;
import org.example.googlesheetservice.Data.RowColumn;
import org.example.postgresql.DAO.PostgreSQLController;
import org.example.postgresql.entity.Employee;
import org.example.postgresql.entity.PackerStatistic;
import org.example.postgresql.entity.SheetId;
import org.example.postgresql.service.EmployeeService;
import org.example.postgresql.service.SheetIdService;
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
    private final SheetIdService sheetIdService;
    private final PostgreSQLController postgres;

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


    public void setRowsOrColumnWidth(int SHEET_ID, RowColumn object, int width, int start, int end) {
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

    private int generateSheetId() {
        Random random = new Random();
        int length = 7 + random.nextInt(5); // 7 + (0..4) = 7..11
        int min = (int) Math.pow(10, length - 1); // Минимальное значение для длины
        int max = (int) Math.pow(10, length) - 1; // Максимальное значение для длины
        return min + random.nextInt(max - min + 1);
    }

    public SheetId createNewSheet() {
        String sheetTitle = getHeaderTitle();

        //ЕСЛИ ТАБЛИЦА С ТАКИМ НАЗВАНИЕМ УЖЕ ЕСТЬ
        int currentSheetId = findSheetIdByTitle(sheetTitle);
        if (currentSheetId != -1) {
            log.info("ТАБЛИЦА С ТАКИМ НАЗВАНИЕМ УЖЕ ЕСТЬ " + sheetTitle);
            sheetIdService.saveSheetId(new SheetId(currentSheetId, sheetTitle));
            return new SheetId(currentSheetId, sheetTitle);
        }

        int sheetId = generateSheetId();
        log.info("SHEET ID = " + sheetId);
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
            log.info(String.format("СОЗДАЛ И СОХРАНИЛ НОВУЮ ТАБЛИЦУ: Title %s, SheetId %d", sheetTitle, sheetId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new SheetId(sheetId, sheetTitle);
    }

    public String createNewSpreadSheet(String title) {
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

    public void mergeCells(GridRange range) {

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


    public void setCellStyle(GridRange range, Color color, String textAlignment, int fontSize, boolean setBold, int borderWidth) {
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

    public void setCellBordersStyle(GridRange range, int borderWidth) {
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


    public void updateTable(SheetId sheetId) {
        int SHEET_ID = sheetId.getSheetId();
        String title = sheetId.getTitle();
        //ОЧИСТКА ВСЕХ ЯЧЕЕК
        cleanTable(sheetId);


        //СЛИЯНИЕ СТОЛБЦОВ ДЛЯ ШАПКИ
        mergeCells(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(1)
                .setEndRowIndex(2)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35));

        //ШИРИНА ТИТУЛЬНИКА 40
        setRowsOrColumnWidth(SHEET_ID, RowColumn.ROWS, 40, 1, 2);
        //ШИРИНА ПЕРВОГО СТОЛБЦА 340
        setRowsOrColumnWidth(SHEET_ID, RowColumn.COLUMNS, 340, 1, 2);
        //ШИРИНА 2-3 СТОЛБЦА 80
        setRowsOrColumnWidth(SHEET_ID, RowColumn.COLUMNS, 80, 2, 4);
        //ШИРИНА ОСТАЛЬНЫХ СТОЛБЦОВ 50
        setRowsOrColumnWidth(SHEET_ID, RowColumn.COLUMNS, 50, 4, 35);

        //ФОРМАТ ВСЕЙ ТАБЛИЦЫ
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(1)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#ffffff"), "CENTER", 11, false, 1);

        //ФОРМАТ ШАПКИ
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(1)
                .setEndRowIndex(2)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#fffede"), "CENTER", 22, true, 2);
        updateData(String.format("%s!B2", title), new ValueRange().setValues(List.of(List.of(getHeaderTitle()))));

        //ФОРМАТ ТЕКСТА ОСНОВНОГО БЛОКА С ФИО
        setCellStyle(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(12 + numberOfDayPrinters + numberOfNightPrinters + marketsNumber)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), getColorByHEX("#ffffff"), "LEFT", 11, true, 1);

        //ФОРМАТ 2 СТРОКИ С ЧИСЛАМИ
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(3)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#ffffff"), "CENTER", 11, true, 1);
        updateData(String.format("%s!C3:AI3", title), new ValueRange().setValues(List.of(List.of("Общее", "Среднее", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"))));

        //ФОРМАТ СТРОКИ "СБОРКА"
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(3)
                .setEndRowIndex(4)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#ffff00"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ СБОРКА"
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(4 + marketsNumber)
                .setEndRowIndex(5 + marketsNumber)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#ffff00"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ДНЕВНАЯ СМЕНА  (Напечатано/Брак)"
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(6 + marketsNumber)
                .setEndRowIndex(7 + marketsNumber)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#76d2a1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ ДЕНЬ"
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(7 + marketsNumber + numberOfDayPrinters)
                .setEndRowIndex(8 + marketsNumber + numberOfDayPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#76d2a1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "НОЧНАЯ СМЕНА"
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(9 + marketsNumber + numberOfDayPrinters)
                .setEndRowIndex(10 + marketsNumber + numberOfDayPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#4dd0e1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ НОЧЬ"
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(10 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setEndRowIndex(11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#4dd0e1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ ПЕЧАТЬ"
        setCellStyle(new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(12 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#d8ffe1"), "LEFT", 11, true, 1);

        //ОСНОВНЫЕ ТЕСТОВЫЕ ПОЛЯ
        updateData(String.format("%s!B4", title), new ValueRange().setValues(List.of(List.of("СБОРКА"))));
        updateData(String.format("%s!B%d", title, 5 + marketsNumber), new ValueRange().setValues(List.of(List.of("ОБЩЕЕ СБОРКА"))));
        updateData(String.format("%s!B%d", title, 7 + marketsNumber), new ValueRange().setValues(List.of(List.of("ДНЕВНАЯ СМЕНА  (Напечатано/Брак)"))));
        updateData(String.format("%s!B%d:B%d", title, 8 + marketsNumber + numberOfDayPrinters, 10 + marketsNumber + numberOfDayPrinters), new ValueRange().setValues(List.of(List.of("ОБЩЕЕ ДЕНЬ"), List.of(""), List.of("НОЧНАЯ СМЕНА"))));
        updateData(String.format("%s!B%d:B%d", title, 11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters, 13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters), new ValueRange().setValues(List.of(List.of("ОБЩЕЕ НОЧЬ"), List.of(""), List.of("ОБЩЕЕ ПЕЧАТЬ"))));

        //ФОРМАТ ОСНОВНОГО БЛОКА С ФИО И ПОКАЗАТЕЛЯМИ
        setCellBordersStyle(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), 2);

        //ФОРМАТ КОЛОНКИ "ОБЩЕЕ"
        setCellBordersStyle(new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(13 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(2)
                .setEndColumnIndex(3), 2);

        //ФОРМАТ КОЛОНКИ "СРЕДНЕЕ"
        setCellBordersStyle(new GridRange()
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
        updateData((String.format("%s!B5:B%d", title, 4 + marketsNumber)), new ValueRange().setValues(markets));

        //ПОЛЯ ДНЕВНЫХ ПЕЧАТНИКОВ
        List<List<Object>> dayPrinters = new ArrayList<>();
        for (int i = 8 + marketsNumber; i < 8 + marketsNumber + numberOfDayPrinters; i++) {
            if (labelNumsList.get(i) != null) {
                dayPrinters.add(List.of(labelNumsList.get(i)));
            }
        }
        updateData((String.format("%s!B%d:B%d", title, 8 + marketsNumber, 7 + marketsNumber + numberOfDayPrinters)), new ValueRange().setValues(dayPrinters));

        //ПОЛЯ НОЧНЫХ ПЕЧАТНИКОВ
        List<List<Object>> nightPrinters = new ArrayList<>();
        for (int i = 11 + marketsNumber + numberOfDayPrinters; i < 11 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters; i++) {
            if (labelNumsList.get(i) != null) {
                nightPrinters.add(List.of(labelNumsList.get(i)));
            }
        }
        updateData((String.format("%s!B%d:B%d", title, 11 + marketsNumber + numberOfDayPrinters, 10 + marketsNumber + numberOfDayPrinters + numberOfNightPrinters)), new ValueRange().setValues(nightPrinters));

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
    public void addPackerStatistic(SheetId sheetId, PackerStatistic statistic) {
        updateLabelList();
        for (Map.Entry<Integer, String> entry : labelNumsList.entrySet()) {
            if (entry.getValue().equals("WB MHC")){
                updateData(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate_column()), entry.getKey()), new ValueRange().setValues(List.of(List.of(statistic.getWb_mhc()))));
            }
            if (entry.getValue().equals("WB Signum")){
                updateData(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate_column()), entry.getKey()), new ValueRange().setValues(List.of(List.of(statistic.getWb_signum()))));

            }
            if (entry.getValue().equals("WB Silicosha")){
                updateData(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate_column()), entry.getKey()), new ValueRange().setValues(List.of(List.of(statistic.getWb_silicosha()))));

            }
            if (entry.getValue().equals("OZON MHC")){
                updateData(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate_column()), entry.getKey()), new ValueRange().setValues(List.of(List.of(statistic.getOzon()))));

            }
            if (entry.getValue().equals("Yandex MHC")){
                updateData(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate_column()), entry.getKey()), new ValueRange().setValues(List.of(List.of(statistic.getYandex()))));

            }
            if (entry.getValue().equals("WB PrintKid")){
                updateData(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate_column()), entry.getKey()), new ValueRange().setValues(List.of(List.of(statistic.getWb_printkid()))));

            }
            if (entry.getValue().equals("FBO")){
                updateData(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate_column()), entry.getKey()), new ValueRange().setValues(List.of(List.of(statistic.getFbo()))));
            }
        }
    }

    public void addPrinterStatistic(SheetId sheetId, PrinterStatistic statistic) {
        int rowNum = 0;
        updateLabelList();
        for (Map.Entry<Integer, String> entry : labelNumsList.entrySet()) {
            if (entry.getValue().equals(statistic.getFio())) {
                rowNum = entry.getKey();
                break;
            }
        }
        if (rowNum != 0) {
            updateData(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate()), rowNum), new ValueRange().setValues(List.of(List.of(String.format("%s/%s", statistic.getPrints_num(), statistic.getDefects_num())))));
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


    public void fullUpdateTable(SheetId sheetId) {
        List<Employee> dayPrintersList = employeeService.getListOfDayPrinters();
        List<Employee> nightPrintersList = employeeService.getListOfNightPrinters();
        List<PackerStatistic> packerStatisticList = postgres.getAllPackerStatistics();
        updateLabelList();
        updateTable(sheetId);
        updateAllStatistic(sheetId, dayPrintersList, nightPrintersList,packerStatisticList);
    }

    private void updateAllStatistic(SheetId sheetId, List<Employee> dayPrintersList, List<Employee> nightPrintersList,List<PackerStatistic> packerStatisticList) {
        updatePrinterStatistic(sheetId, dayPrintersList);
        updatePrinterStatistic(sheetId, nightPrintersList);
        updatePackerStatistic(sheetId, packerStatisticList);
    }

    private void updatePackerStatistic(SheetId sheetId, List<PackerStatistic> packerStatisticList) {
        for (PackerStatistic packerStatistic : packerStatisticList) {
            addPackerStatistic(sheetId, packerStatistic);
        }
    }

    private void updatePrinterStatistic(SheetId sheetId, List<Employee> printersList) {
        for (Employee employee : printersList) {
            for (Map.Entry<Integer, String> entry : labelNumsList.entrySet()) {
                if (entry.getValue().equals(employee.getFio())) {
                    int rowNum = entry.getKey();
                    List<org.example.postgresql.entity.PrinterStatistic> stat_list = postgres.getPrinterStatisticByChatId(employee.getChatId());
                    if (stat_list != null) {
                        for (org.example.postgresql.entity.PrinterStatistic statistic : stat_list) {
                            updateData(String.format("%s!%s%d", sheetId.getTitle(), getColumnLetter(statistic.getDate()), rowNum), new ValueRange().setValues(List.of(List.of(String.format("%s/%s", statistic.getPrints_num(), statistic.getDefects_num())))));
                        }
                    }
                }
            }
        }
    }





}
