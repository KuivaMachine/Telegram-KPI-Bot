package org.example.kpitelegrambot.service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

public interface DeleteService {
    DeleteMessage deleteLastBotsMessage(Long chatId, Integer messageId);
    void saveMessageId(int chatId, int messageId);
    int getLastMessageId(int chatId);
}
