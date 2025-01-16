package org.example.kpitelegrambot.bot.handlers;

import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.kpitelegrambot.entity.Employee;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface JobHandler{
    SendMessage process(TelegramBot bot, Update update, Employee employee, SendMessage message);
}
