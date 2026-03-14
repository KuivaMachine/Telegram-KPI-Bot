package org.example.kpitelegrambot.data.service;

import lombok.experimental.UtilityClass;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


/**
 * Утилитный класс для работы с датами
 */
public class DateService {
    private static final LocalDate localDate = LocalDate.now();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Проверяет валидность даты в строковом формате
     * @param dateStr строка с датой в формате "dd-MM-yyyy"
     * @return true если дата валидна
     */
    public static boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * @return текущую дату в формате "dd-MM-yyyy"
     */
    public static String getCurrentDate() {
        return localDate.format(formatter);
    }

    /**
     * @return вчерашнюю дату в формате "dd-MM-yyyy"
     */
    public static String getYesterdayDate() {
        return localDate.minusDays(1).format(formatter);
    }

    /**
     * @return позавчерашнюю дату в формате "dd-MM-yyyy"
     */
    public static String getDBYDate() {
        return localDate.minusDays(2).format(formatter);
    }


    /**
     * Преобразует дату формата "dd-MM-yyyy" в объект класса java.sql.Date
     * @param date дата
     * @return объект java.sql.Date
     */
    public static Date parseStringToSqlDate(String date) {
        return Date.valueOf(LocalDate.parse(date, formatter));
    }

    /**Преобразует дату формата (format) в объект java.time.LocalDate
     * @param date дата
     * @param format формат
     * @return объект класса java.time.LocalDate
     */
    public static LocalDate parseStringToLocalDate(String date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(date, formatter);
    }

    /**
     * @return текущую дату в виде объекта java.sql.Date
     */
    public static Date getLocalDate() {
        LocalDate localDate = LocalDate.now();
        return Date.valueOf(localDate);
    }

    /**
     * Преобразует дату из формата SQL "yyyy-MM-dd" в строку формата "dd.MM.yyyy"
     * @param date дата
     * @return строка формата "dd.MM.yyyy"
     */
    public static String parseSqlDateToString(String date) {
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.parse(date, formatter1).format(formatter2);
    }
}
