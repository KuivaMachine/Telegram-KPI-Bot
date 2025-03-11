package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.bot.keyboards.InlineKeyboardFactory;
import org.example.kpitelegrambot.bot.keyboards.ReplyKeyboardFactory;
import org.example.kpitelegrambot.data.AnswersList;
import org.example.kpitelegrambot.data.ButtonLabels;
import org.example.kpitelegrambot.googlesheets.KafkaProducer;
import org.example.kpitelegrambot.postgresql.DAO.PostgreSQLController;
import org.example.kpitelegrambot.postgresql.data.DayNight;
import org.example.kpitelegrambot.postgresql.data.EmployeePost;
import org.example.kpitelegrambot.postgresql.data.EmployeeStatus;
import org.example.kpitelegrambot.postgresql.entity.Employee;
import org.example.kpitelegrambot.postgresql.entity.PrinterStatistic;
import org.example.kpitelegrambot.postgresql.service.DateService;
import org.example.kpitelegrambot.postgresql.service.EmployeeService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j2
@Component
@RequiredArgsConstructor
public class CallbackQueryHandler implements Handler {

    private final EmployeeService employeeService;
    private final PostgreSQLController postgres;
    private final KafkaProducer kafkaProducer;

    DateService dateService = new DateService();

    @Override
    public SendMessage process(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callback = update.getCallbackQuery().getData();
        SendMessage sendMessage = new SendMessage();
        Employee currentEmployee = employeeService.getEmployeeByChatId(chatId);

        sendMessage.setChatId(chatId);
        sendMessage.setText("Эта команда сейчас неактивна) Если нужна помощь - попробуйте <b>/help</b>");
        sendMessage.setParseMode("HTML");

        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_JOB)) {
            if (callback.equals(ButtonLabels.I_AM_PACKER.getCallback())) {
                return addNewPacker(sendMessage, currentEmployee);
            }
            if (callback.equals(ButtonLabels.I_AM_PRINTER.getCallback())) {
                return fillWorkTimeProcess(sendMessage, currentEmployee);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_WORKTIME)) {
            if (callback.equals(DayNight.DAY.getCallback())) {
                return addNewPrinter(sendMessage, currentEmployee, DayNight.DAY);
            }
            if (callback.equals(DayNight.NIGHT.getCallback())) {
                return addNewPrinter(sendMessage, currentEmployee, DayNight.NIGHT);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_DATE)) {
            if (callback.matches("^\\d{2}-\\d{2}-\\d{4}$")) {
                return fillDateProcess(callback, currentEmployee, sendMessage);
            }
            if(callback.equals(ButtonLabels.ANOTHER_DATE.getCallback())){
                return anotherDateProcess(currentEmployee, sendMessage);
            }
        }

        return sendMessage;
    }

    private SendMessage anotherDateProcess(Employee currentEmployee, SendMessage sendMessage) {
        currentEmployee.setStatus(EmployeeStatus.WAITING_ANOTHER_DATE);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.ENTER_ANOTHER_DATE.getText());
        return sendMessage;
    }


    private SendMessage fillWorkTimeProcess(SendMessage sendMessage, Employee currentEmployee) {

        currentEmployee.setStatus(EmployeeStatus.WAITING_WORKTIME);
        employeeService.save(currentEmployee);
        sendMessage.setText("""
                В дневную или в ночную смену?)
                """);
        sendMessage.setReplyMarkup(InlineKeyboardFactory.getDayNightKeyboard());
        return sendMessage;
    }

    SendMessage fillDateProcess(String callback, Employee currentEmployee, SendMessage sendMessage) {
        String nicePhrase;
        postgres.addValueInBufferFromPrinter(currentEmployee, dateService.parseStringToSqlDate(callback), "date");
        nicePhrase = postgres.getNicePhraseToPrinter(currentEmployee);
        PrinterStatistic addedStat = postgres.moveDataFromPrinterBufferToMainTable(currentEmployee);
        if (addedStat != null) {
            currentEmployee.setStatus(EmployeeStatus.SAVED);
            employeeService.save(currentEmployee);
            sendMessage.setText(String.format("Я все записал!\n%s", nicePhrase));
            kafkaProducer.send("printer_stat_topic", addedStat);
        }else{
            sendMessage.setText("У меня не очень получилось записать :(\nМожет, попробовать еще раз?");
        }
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getShowAndAddKeyboard());
        return sendMessage;
    }

    private SendMessage addNewPrinter(SendMessage sendMessage, Employee currentEmployee, DayNight workTime) {
        currentEmployee.setWorkTime(workTime);
        currentEmployee.setJob(EmployeePost.PRINTER);
        currentEmployee.setStatus(EmployeeStatus.SAVED);
        employeeService.save(currentEmployee);
        kafkaProducer.send("commands", "UPDATE");
        sendMessage.setText("""
                Отлично 👍 Чтобы записать статистику,\s
                нажмите «Добавить статистику»
                """);
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getAddStatKeyboard());
        return sendMessage;
    }

    private SendMessage  addNewPacker(SendMessage sendMessage, Employee currentEmployee) {
        currentEmployee.setJob(EmployeePost.PACKER);
        currentEmployee.setStatus(EmployeeStatus.SAVED);
        employeeService.save(currentEmployee);
        if (kafkaProducer.send("commands", "UPDATE")){
            sendMessage.setText("""
                Отлично 👍 Чтобы записать статистику,\s
                нажмите «Добавить новую статистику»
                """);
            sendMessage.setReplyMarkup(ReplyKeyboardFactory.getAddStatKeyboard());
        }else{
            sendMessage.setText("""
                Отлично 👍 Чтобы записать статистику,\s
                нажмите «Добавить новую статистику»
                На сервере небольшая проблемка, скиньте скриншот сообщения ему - @olezha_zaostrovtsev
                """);
        }
        return sendMessage;
    }


}
