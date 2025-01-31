package org.example.postgresql.service;

import org.example.postgresql.entity.SheetId;

public interface SheetIdService {
    void saveSheetId(SheetId sheetId);
    SheetId findByTitle(String title);
}
