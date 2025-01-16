package org.example.kpitelegrambot.bot;


import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.bot.configuration.TelegramBotConfig;
import org.example.kpitelegrambot.bot.handlers.CallbackQueryHandler;
import org.example.kpitelegrambot.bot.handlers.UpdateHandler;
import org.example.kpitelegrambot.data.ButtonLabels;
import org.example.kpitelegrambot.data.DayNight;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

//TODO НАДО ДОБАВИТЬ МЕТОД НА СОЗДАНИЕ ТАБЛИЦЫ ПЕЧАТНИКА

@Log4j2
@Component

public class TelegramBot extends TelegramLongPollingBot {

    CallbackQueryHandler callbackQueryHandler;
    UpdateHandler updateHandler;
    TelegramBotConfig telegramBotConfig;


    public TelegramBot(CallbackQueryHandler callbackQueryHandler, UpdateHandler updateHandler, TelegramBotConfig telegramBotConfig) {
        super(telegramBotConfig.getToken());

        this.callbackQueryHandler = callbackQueryHandler;
        this.updateHandler = updateHandler;
        this.telegramBotConfig = telegramBotConfig;
        updateHandler.register(this);

        try {
            //ДЛЯ LONGPOLLING
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
            //ДЛЯ WEBHOOK
            //SetWebhook setWebhook = SetWebhook.builder().url(telegramBotConfig.getUrl()).build();
            //this.setWebhook(setWebhook);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

  /*  @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return updateProcessor.processUpdate(update);
    }

    @Override
    public String getBotPath() {
        return "/update";
    }*/


    @Override
    public String getBotUsername() {
        return telegramBotConfig.getName();
    }


    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals(ButtonLabels.I_AM_PACKER.getCallback()) || update.getCallbackQuery().getData().equals(ButtonLabels.I_AM_PRINTER.getCallback())||update.getCallbackQuery().getData().equals(DayNight.DAY.getCallback())||update.getCallbackQuery().getData().equals(DayNight.NIGHT.getCallback())) {
                deleteLastMessage(update.getCallbackQuery().getMessage().getChatId(), updateHandler.getLastMessageId(update.getCallbackQuery().getMessage().getChatId()));
            }
            sendAnswer(callbackQueryHandler.process(update));
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            sendAnswer(updateHandler.process(update));
        }
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

    private void sendAnswer(SendMessage sendMessage) {
        int messageId;
        try {
            messageId = execute(sendMessage).getMessageId();
            updateHandler.saveLastMessageId(sendMessage.getChatId(), messageId);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

}

