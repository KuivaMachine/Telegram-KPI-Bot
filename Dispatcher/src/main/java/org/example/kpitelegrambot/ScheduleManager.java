package org.example.kpitelegrambot;

import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.kpitelegrambot.bot.configuration.SettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class ScheduleManager {
    int count = 0;
    TelegramBot bot;

    @Autowired
    SettingsManager settingsManager;
    public void init(TelegramBot bot) {
        this.bot = bot;
    }


    @Scheduled(cron = "0 0 13 * * ?", zone = "Europe/Moscow")
    public void runDailyTask() {
        SendMessage message = new SendMessage();
        message.setChatId(889218535L);
        message.setText("Я жив, дней без перезагрузки - "+count);
        if (settingsManager.isNotificationEnabled()) {
            try {
                bot.execute(message);
                count++;
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
