package org.example.kpitelegrambot.DAO.entity;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults (level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@Setter
@RequiredArgsConstructor
public class PackerStatistic {
    String date_column;
    String wb_mhc;
    String wb_signum;
    String wb_silicosha;
    String ozon;
    String yandex;
    String wb_printkid;
    String fbo;
}
