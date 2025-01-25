package org.example.kpitelegrambot.service;

import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class DateService {
    LocalDate localDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public String getCurrentDate() {
        return localDate.format(formatter);
    }

    public String getYesterdayDate() {
        return localDate.minusDays(1).format(formatter);
    }

    public String getDBYDate() {
        return localDate.minusDays(2).format(formatter);
    }
    public Date parseStringToSqlDate(String date) {
        LocalDate localDate = LocalDate.parse(date, formatter);
        return Date.valueOf(localDate);
    }
    public java.sql.Date getLocalDate() {
        LocalDate localDate = LocalDate.now();
        return Date.valueOf(localDate);
    }
    public static String parseSqlDateToString(String date){
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.parse(date, formatter1).format(formatter2);
    }
}
