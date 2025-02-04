package org.example.googlesheetservice.SheetsServices;


import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.example.googlesheetservice.Data.Months;
import org.example.googlesheetservice.Data.PrinterStatistic;
import org.example.googlesheetservice.Data.RowColumn;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Log4j2
@Component
public class GoogleSheetsService {

    public final Sheets sheetService;

    private String SPREADSHEET_ID = "1NpExJ1FOSxgpkPRFmR5q0lKCeIfD0vujReHkwynY_tY";
    private final int SHEET_ID = 1962952644;


    public GoogleSheetsService(Sheets sheetService) {
        this.sheetService = sheetService;
    }


  /*  @PostConstruct
    public void init() {
        try {
            var list = sheetService.spreadsheets().get(SPREADSHEET_ID).execute().getSpreadsheetUrl();
            System.out.println(list);
            // createNewSheet(13,12);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

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

    public void updateData(String range, com.google.api.services.sheets.v4.model.ValueRange data) {
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

    public void createNewSheet(int numberOfDayPrinters, int numberOfNightPrinters) throws GeneralSecurityException, IOException {


//        addNewSheet(sheetService, SHEET_NAME);
//        this.SHEET_ID = findSheetIdByTitle(SHEET_NAME);
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
        //ШИРИНА ОСТАЛЬНЫХ СТОЛБЦОВ 45
        setRowsOrColumnWidth(sheetService, RowColumn.COLUMNS, 45, 4, 35);

        //ФОРМАТ ВСЕЙ ТАБЛИЦЫ
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(1)
                .setEndRowIndex(19 + numberOfDayPrinters + numberOfNightPrinters)
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
                .setEndRowIndex(35)
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
                .setStartRowIndex(11)
                .setEndRowIndex(12)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#ffff00"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ПЕЧАТЬ"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(13)
                .setEndRowIndex(14)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#76d2a1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ ДЕНЬ"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex(14 + numberOfDayPrinters)
                .setEndRowIndex(14 + numberOfDayPrinters + 1)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#76d2a1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "НОЧНАЯ СМЕНА"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex((14 + numberOfDayPrinters - 1) + 3)
                .setEndRowIndex((14 + numberOfDayPrinters - 1) + 4)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#4dd0e1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ НОЧЬ"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex((14 + numberOfDayPrinters - 1) + 4 + numberOfNightPrinters)
                .setEndRowIndex((14 + numberOfDayPrinters - 1) + 4 + numberOfNightPrinters + 1)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#4dd0e1"), "LEFT", 11, true, 1);

        //ФОРМАТ СТРОКИ "ОБЩЕЕ ПЕЧАТЬ"
        setCellStyle(sheetService, new GridRange().setSheetId(SHEET_ID)
                .setStartRowIndex((14 + numberOfDayPrinters - 1) + 4 + numberOfNightPrinters + 2)
                .setEndRowIndex((14 + numberOfDayPrinters - 1) + 4 + numberOfNightPrinters + 3)
                .setStartColumnIndex(1)
                .setEndColumnIndex(35), getColorByHEX("#d8ffe1"), "LEFT", 11, true, 1);

        //ОСНОВНЫЕ ТЕСТОВЫЕ ПОЛЯ
        updateData("B3:B14", new ValueRange().setValues(List.of(List.of(""), List.of("СБОРКА"), List.of("WB MHC"), List.of("WB Signum"), List.of("WB Silicosha"), List.of("OZON MHC"), List.of("Yandex MHC"), List.of("WB PrintKid"), List.of("FBO"), List.of("ОБЩЕЕ СБОРКА"), List.of(""), List.of("ДНЕВНАЯ СМЕНА  (Напечатано/Брак)"))));
        updateData(String.format("B%d:B%d", 15 + numberOfDayPrinters, 15 + numberOfDayPrinters + 2), new ValueRange().setValues(List.of(List.of("ОБЩЕЕ ДЕНЬ"), List.of(""), List.of("НОЧНАЯ СМЕНА"))));
        updateData(String.format("B%d:B%d", (15 + numberOfDayPrinters - 1) + 4 + numberOfNightPrinters, (15 + numberOfDayPrinters - 1) + 4 + numberOfNightPrinters + 2), new ValueRange().setValues(List.of(List.of("ОБЩЕЕ НОЧЬ"), List.of(""), List.of("ОБЩЕЕ ПЕЧАТЬ"))));

        //ФОРМАТ ОСНОВНОГО БЛОКА С ФИО И ПОКАЗАТЕЛЯМИ
        setCellBordersStyle(sheetService, new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(20 + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(1)
                .setEndColumnIndex(2), 2);

        //ФОРМАТ КОЛОНКИ "ОБЩЕЕ"
        setCellBordersStyle(sheetService, new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(20 + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(2)
                .setEndColumnIndex(3), 2);

        //ФОРМАТ КОЛОНКИ "СРЕДНЕЕ"
        setCellBordersStyle(sheetService, new GridRange()
                .setSheetId(SHEET_ID)
                .setStartRowIndex(2)
                .setEndRowIndex(20 + numberOfDayPrinters + numberOfNightPrinters)
                .setStartColumnIndex(3)
                .setEndColumnIndex(4), 2);
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

    }

}
