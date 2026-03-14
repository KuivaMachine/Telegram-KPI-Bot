package org.example.kpitelegrambot.bot;


import org.example.kpitelegrambot.bot.configuration.TelegramBotConfig;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


//@Component
public class TelegramBotLongPolling extends TelegramLongPollingBot {

    private final TelegramBotConfig telegramBotConfig;
    private final TelegramRestController telegramRestController;

    public TelegramBotLongPolling(TelegramRestController telegramRestController, TelegramBotConfig telegramBotConfig) throws TelegramApiException {
        super(telegramBotConfig.getToken());
        this.telegramBotConfig = telegramBotConfig;
        this.telegramRestController = telegramRestController;

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this);
    }


    @Override
    public String getBotUsername() {
        return telegramBotConfig.getName();
    }


    @Override
    public void onUpdateReceived(Update update) {try {
            execute(telegramRestController.receiveUpdate(update));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}

