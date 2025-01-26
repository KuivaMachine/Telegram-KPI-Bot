package org.example.kpitelegrambot.bot.handlers;

import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.postgresql.entity.Employee;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface JobHandler{
    SendMessage process(TelegramBot bot, Update update, Employee employee, SendMessage message);
    SendMessage cancelAddingStatistic(SendMessage sendMessage, Employee currentEmployee);
    SendMessage invalidNumberProcess(SendMessage sendMessage, String text);
    SendMessage showLastRecord(Employee currentEmployee, SendMessage sendMessage);
    SendMessage createNewStatisticPost(Employee currentEmployee, SendMessage sendMessage);
    SendMessage sayHelloProcess(SendMessage sendMessage, Employee currentEmployee);
}
