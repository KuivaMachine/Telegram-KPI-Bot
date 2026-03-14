package org.example.kpitelegrambot.data.service;

import org.example.kpitelegrambot.data.entity.SheetId;

public interface SheetIdService {
    void saveSheetId(SheetId sheetId);
    SheetId findSheetIdByTitle(String title);
}
