package org.example.kpitelegrambot;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.bot.TelegramBotWebHook;
import org.example.kpitelegrambot.bot.configuration.SettingsManager;
import org.example.kpitelegrambot.data.service.LogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class ScheduleManager {
    int count = 0;
    TelegramBotWebHook bot;
    private final SettingsManager settingsManager;
    private final LogService log;

    public void init(TelegramBotWebHook bot) {
        this.bot = bot;
    }
    /**
     Отправляет сообщение в чат 889218535 каждый день в 13:00 по МСК, если разрешен флаг isNotificationEnabled()
     */
    @Scheduled(cron = "0 0 13 * * ?", zone = "Europe/Moscow")
    public void runDailyTask() {
        SendMessage message = new SendMessage();
        count++;
        message.setChatId("889218535");
        message.setText("Я жив, дней без перезагрузки - "+count);
        if (settingsManager.isNotificationEnabled()) {
            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообщения от runDailyTask: " + e.getMessage());
            }
        }

    }
}
