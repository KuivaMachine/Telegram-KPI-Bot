package org.example.kpitelegrambot.bot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;

public class ReplyKeyboardFactory {

    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

    public static ReplyKeyboardMarkup getAddStatKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Добавить статистику"));

        return getOneRowKeyboard(row1);
    }


    public static ReplyKeyboardMarkup getDeleteStatKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("УДАЛИТЬ ВСЕ"));

        return getOneRowKeyboard(row1);
    }

    private static ReplyKeyboardMarkup getOneRowKeyboard(KeyboardRow row1) {
        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }
}
