package org.example.kpitelegrambot.bot.keyboards;

import org.example.kpitelegrambot.data.ButtonLabels;
import org.example.postgresql.data.DayNight;
import org.example.postgresql.service.DateService;
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
        DateService dateService = new DateService();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton dayBefYesBut = new InlineKeyboardButton();
        dayBefYesBut.setText(dateService.getCurrentDate());
        dayBefYesBut.setCallbackData(dateService.getCurrentDate());
        row1.add(dayBefYesBut);

        InlineKeyboardButton yesterdayBut = new InlineKeyboardButton();
        yesterdayBut.setText(dateService.getYesterdayDate());
        yesterdayBut.setCallbackData(dateService.getYesterdayDate());
        row1.add(yesterdayBut);

        InlineKeyboardButton todayBut = new InlineKeyboardButton();
        todayBut.setText(dateService.getDBYDate());
        todayBut.setCallbackData(dateService.getDBYDate());
        row1.add(todayBut);

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

    public  static InlineKeyboardMarkup getShowReceivedStatistic(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton show = new InlineKeyboardButton();
        show.setText(ButtonLabels.SHOW_STATISTIC.getLabel());
        show.setCallbackData(ButtonLabels.SHOW_STATISTIC.getCallback());

        row1.add(show);
        rows.add(row1);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

}
