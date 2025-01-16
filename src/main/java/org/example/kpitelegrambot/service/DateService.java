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
        localDate = localDate.minusDays(1);
        return localDate.format(formatter);
    }

    public String getDBYDate() {
        localDate = localDate.minusDays(2);
        return localDate.format(formatter);
    }
    public Date getSqlDate(String date) {
        LocalDate localDate = LocalDate.parse(date, formatter);
        return Date.valueOf(localDate);
    }
}
