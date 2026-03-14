package org.example.kpitelegrambot.data.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.data.entity.SheetId;
import org.example.kpitelegrambot.data.repository.SheetIdRepository;
import org.example.kpitelegrambot.data.service.LogService;
import org.example.kpitelegrambot.data.service.SheetIdService;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SheetIdServiceImpl implements SheetIdService {


    SheetIdRepository sheetIdRepository;
    LogService log;

    @Override
    public void saveSheetId(SheetId sheetId) {
        sheetIdRepository.save(sheetId);
    }

    @Override
    public SheetId findSheetIdByTitle(String title) {
        Optional<SheetId> sheetId = sheetIdRepository.findByTitle(title);
        if (sheetId.isPresent()) {
            return sheetId.get();
        }else{
            log.error("SHEET ID DID NOT FOUND by title " + title);
            return null;
        }
    }

}
