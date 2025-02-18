package org.example.googlesheetservice.postgresql.service;


import org.example.googlesheetservice.postgresql.entity.SheetId;

public interface SheetIdService {
    void saveSheetId(SheetId sheetId);
    SheetId findByTitle(String title);
}
