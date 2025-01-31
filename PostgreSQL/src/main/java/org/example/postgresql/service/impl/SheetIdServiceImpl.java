package org.example.postgresql.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.postgresql.entity.SheetId;
import org.example.postgresql.entity.repository.SheetIdRepository;
import org.example.postgresql.service.SheetIdService;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Log4j2
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SheetIdServiceImpl implements SheetIdService {


    SheetIdRepository sheetIdRepository;

    @Override
    public void saveSheetId(SheetId sheetId) {
        sheetIdRepository.save(sheetId);
        log.info("SAVED SHEET ID "+ sheetId);
    }

    @Override
    public int findByTitle(String title) {
        Optional<SheetId> sheetId = sheetIdRepository.findByTitle(title);
        if (sheetId.isPresent()) {
            return sheetId.get().getSheetId();
        }else {
            return -1;
        }
    }

}
