package org.example.kpitelegrambot.bot;


import org.example.kpitelegrambot.ScheduleManager;
import org.example.kpitelegrambot.bot.configuration.TelegramBotConfig;
import org.example.kpitelegrambot.bot.handlers.UpdateHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class TelegramBot extends TelegramWebhookBot {


    TelegramBotConfig telegramBotConfig;
    TelegramRestController telegramRestController;
    ScheduleManager taskManager;
    public TelegramBot(ScheduleManager taskManager, TelegramRestController telegramRestController, UpdateHandler updateHandler, TelegramBotConfig telegramBotConfig) {
        super(telegramBotConfig.getToken());
        this.telegramBotConfig = telegramBotConfig;
        this.telegramRestController = telegramRestController;
        this.taskManager = taskManager;
        updateHandler.register(this);
        taskManager.init(this);
        try {
            SetWebhook setWebhook = SetWebhook.builder().url(telegramBotConfig.getUrl()).build();
            this.setWebhook(setWebhook);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return telegramRestController.receiveUpdate(update);
    }

    @Override
    public String getBotPath() {
        return "/bot_kpi/update";
    }


    @Override
    public String getBotUsername() {
        return telegramBotConfig.getName();
    }


    public void deleteLastMessage(Long chatId, int lastMessageId) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setMessageId(lastMessageId);
            deleteMessage.setChatId(chatId);
            execute(deleteMessage);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}

