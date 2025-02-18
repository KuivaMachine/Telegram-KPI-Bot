package org.example.kpitelegrambot.postgresql.service;

import org.example.kpitelegrambot.postgresql.entity.SheetId;

public interface SheetIdService {
    void saveSheetId(SheetId sheetId);
    SheetId findByTitle(String title);
}
