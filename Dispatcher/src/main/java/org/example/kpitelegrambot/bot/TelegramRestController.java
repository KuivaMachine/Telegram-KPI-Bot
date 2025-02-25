package org.example.kpitelegrambot.bot;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.kpitelegrambot.bot.handlers.CallbackQueryHandler;
import org.example.kpitelegrambot.bot.handlers.UpdateHandler;
import org.example.kpitelegrambot.data.AnswersList;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
@FieldDefaults (level = AccessLevel.PRIVATE, makeFinal = true)
public class TelegramRestController {

    CallbackQueryHandler callbackQueryHandler;
    UpdateHandler updateHandler;

    @PostMapping ("/callback/update")
    public BotApiMethod<?> receiveUpdate(@RequestBody Update update) {
        if (update.hasCallbackQuery()) {
           return callbackQueryHandler.process(update);
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
           return updateHandler.process(update);
        }
        if (update.hasEditedMessage()){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText(AnswersList.CHANGE_MESSAGE.getText());
            return sendMessage;
        }
        return null;
    }
}
