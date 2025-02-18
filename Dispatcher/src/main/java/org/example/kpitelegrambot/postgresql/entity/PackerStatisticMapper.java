package org.example.kpitelegrambot.postgresql.entity;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PackerStatisticMapper implements RowMapper<PackerStatistic> {
    @Override
    public PackerStatistic mapRow(ResultSet rs, int rowNum) throws SQLException {
        String date = rs.getString("date");
        String wb_mhc = rs.getString("wb_mhc");
        String wb_signum = rs.getString("wb_signum");
        String wb_silicosha =rs.getString("wb_silicosha"); ;
        String ozon = rs.getString("ozon");
        String yandex = rs.getString("yandex");
        String wb_printkid = rs.getString("wb_printkid");
        String fbo = rs.getString("fbo");
        return new PackerStatistic(date, wb_mhc, wb_signum, wb_silicosha, ozon, yandex, wb_printkid, fbo);
    }
}
