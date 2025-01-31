package org.example.postgresql.service;

import org.example.postgresql.entity.SheetId;

import java.util.Optional;

public interface SheetIdService {
    void saveSheetId(SheetId sheetId);
    int findByTitle(String title);
}
