package org.example.postgresql.entity;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PrinterStatisticMapper implements RowMapper<PrinterStatistic> {
    @Override
    public PrinterStatistic mapRow(ResultSet rs, int rowNum) throws SQLException {
        String date = rs.getString("date");
        String prints_num = rs.getString("prints_num");
        String defects_num = rs.getString("defects_num");

        return new PrinterStatistic(date, prints_num, defects_num, "fio");
    }
}
