package org.example.kpitelegrambot.bot.keyboards;

import org.example.kpitelegrambot.data.ButtonLabels;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class ReplyKeyboardFactory {

    public static ReplyKeyboardMarkup getAddStatKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(ButtonLabels.ADD_NEW_STATISTICS.getLabel()));
        return createKeyboard(List.of(row1));
    }

    private static ReplyKeyboardMarkup createKeyboard(List<KeyboardRow> list) {
        ArrayList<KeyboardRow> keyboard = new ArrayList<>(list);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }

    public static ReplyKeyboard getCancelKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(ButtonLabels.CANCEL_ADDING.getLabel()));
        return createKeyboard(List.of(row1));
    }

    public static ReplyKeyboardMarkup getShowAndAddKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(ButtonLabels.ADD_NEW_STATISTICS.getLabel()));
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(ButtonLabels.SHOW_STATISTIC.getLabel()));
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(ButtonLabels.DELETE_LAST_RECORD.getLabel()));
        return createKeyboard(List.of(row1, row2, row3));
    }

    public static ReplyKeyboard getYesNoMarkup() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(ButtonLabels.YES.getLabel()));
        row1.add(new KeyboardButton(ButtonLabels.NO.getLabel()));
        return createKeyboard(List.of(row1));
    }
}
