package org.example.kpitelegrambot.bot.keyboards;

import org.example.kpitelegrambot.data.ButtonLabels;
import org.example.kpitelegrambot.postgresql.data.DayNight;
import org.example.kpitelegrambot.postgresql.service.DateService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyboardFactory {


    public static InlineKeyboardMarkup getJobChoiceKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton printerButton = new InlineKeyboardButton();
          printerButton.setText(ButtonLabels.I_AM_PRINTER.getLabel());
          printerButton.setCallbackData(ButtonLabels.I_AM_PRINTER.getCallback());
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(printerButton);

        InlineKeyboardButton packerButton = new InlineKeyboardButton();
        packerButton.setText(ButtonLabels.I_AM_PACKER.getLabel());
        packerButton.setCallbackData(ButtonLabels.I_AM_PACKER.getCallback());
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(packerButton);

        keyboard.add(row1);
        keyboard.add(row2);

       inlineKeyboardMarkup.setKeyboard(keyboard);
       return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getDateChoiceKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();


        InlineKeyboardButton dayBefYesBut = new InlineKeyboardButton();
        dayBefYesBut.setText(DateService.getCurrentDate());
        dayBefYesBut.setCallbackData(DateService.getCurrentDate());
        row1.add(dayBefYesBut);

        InlineKeyboardButton yesterdayBut = new InlineKeyboardButton();
        yesterdayBut.setText(DateService.getYesterdayDate());
        yesterdayBut.setCallbackData(DateService.getYesterdayDate());
        row1.add(yesterdayBut);

        InlineKeyboardButton todayBut = new InlineKeyboardButton();
        todayBut.setText(DateService.getDBYDate());
        todayBut.setCallbackData(DateService.getDBYDate());
        row1.add(todayBut);

        InlineKeyboardButton anotherDateBut = new InlineKeyboardButton();
        anotherDateBut.setText(ButtonLabels.ANOTHER_DATE.getLabel());
        anotherDateBut.setCallbackData(ButtonLabels.ANOTHER_DATE.getCallback());
        row1.add(anotherDateBut);

        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows); // Устанавливаем клавиатуру
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getDayNightKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton dayButton = new InlineKeyboardButton();
        dayButton.setText(DayNight.DAY.getLabel());
        dayButton.setCallbackData(DayNight.DAY.getCallback());
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(dayButton);

        InlineKeyboardButton nightButton = new InlineKeyboardButton();
        nightButton.setText(DayNight.NIGHT.getLabel());
        nightButton.setCallbackData(DayNight.NIGHT.getCallback());
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(nightButton);

        keyboard.add(row1);
        keyboard.add(row2);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

}
