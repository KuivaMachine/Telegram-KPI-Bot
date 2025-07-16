package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.bot.keyboards.InlineKeyboardFactory;
import org.example.kpitelegrambot.bot.keyboards.ReplyKeyboardFactory;
import org.example.kpitelegrambot.data.AnswersList;
import org.example.kpitelegrambot.data.ButtonLabels;
import org.example.kpitelegrambot.googlesheets.StatisticHandler;
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
import java.util.concurrent.CompletableFuture;

/**
 * Обработчик событий от callback кнопок inline-клавиатуры
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class CallbackQueryHandler implements Handler {

    private final EmployeeService employeeService;
    private final PostgreSQLController postgres;
    private final StatisticHandler statisticHandler;

    /**
     * Основной обработчик события.
     * @param update объект события
     * @return сообщение класса SendMessage
     */
    @Override
    public SendMessage process(Update update) {
        // ДОСТАЕМ CHAT_ID И CALLBACK ИЗ UPDATE, НАХОДИМ ПОЛЬЗОВАТЕЛЯ
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callback = update.getCallbackQuery().getData();
        SendMessage sendMessage = new SendMessage();
        Employee currentEmployee = employeeService.getEmployeeByChatId(chatId);
        // ДОСТАЕМ CHAT_ID И CALLBACK ИЗ UPDATE, НАХОДИМ ПОЛЬЗОВАТЕЛЯ
        sendMessage.setChatId(chatId);
        sendMessage.setText(AnswersList.CALLBACK_INVALID_COMMAND.getText());
        sendMessage.setParseMode("HTML");
        // ЕСЛИ ОЖИДАЕТСЯ ВВОД ДОЛЖНОСТИ
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_JOB)) {
            if (callback.equals(ButtonLabels.I_AM_PACKER.getCallback())) {
                return addNewPacker(sendMessage, currentEmployee);
            }
            if (callback.equals(ButtonLabels.I_AM_PRINTER.getCallback())) {
                return fillWorkTimeProcess(sendMessage, currentEmployee);
            }
        }
        // ЕСЛИ ОЖИДАЕТСЯ ВВОД РАБОЧЕГО ВРЕМЕНИ (ДЕНЬ/НОЧЬ)
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_WORKTIME)) {
            if (callback.equals(DayNight.DAY.getCallback())) {
                return addNewPrinter(sendMessage, currentEmployee, DayNight.DAY);
            }
            if (callback.equals(DayNight.NIGHT.getCallback())) {
                return addNewPrinter(sendMessage, currentEmployee, DayNight.NIGHT);
            }
        }
        // ЕСЛИ ОЖИДАЕТСЯ ВВОД ДАТЫ
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

    /**
     * Предлагает пользователю ввести свою дату
     * @param currentEmployee пользователь
     * @param sendMessage сообщение
     * @return сообщение SendMessage
     */
    private SendMessage anotherDateProcess(Employee currentEmployee, SendMessage sendMessage) {
        currentEmployee.setStatus(EmployeeStatus.WAITING_ANOTHER_DATE);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.ENTER_ANOTHER_DATE.getText());
        return sendMessage;
    }


    /**
     * Предлагает пользователю ввести время работы (день или ночь)
     * @param currentEmployee пользователь
     * @param sendMessage сообщение
     * @return сообщение SendMessage
     */
    private SendMessage fillWorkTimeProcess(SendMessage sendMessage, Employee currentEmployee) {
        currentEmployee.setStatus(EmployeeStatus.WAITING_WORKTIME);
        employeeService.save(currentEmployee);
        sendMessage.setText("""
                В дневную или в ночную смену?)
                """);
        sendMessage.setReplyMarkup(InlineKeyboardFactory.getDayNightKeyboard());
        return sendMessage;
    }

    /**
     * Обрабатывает callback даты (добавление статистики печатника)
     * @param callback дата
     * @param currentEmployee пользователь
     * @param sendMessage сообщение
     * @return сообщение SendMessage
     */
    SendMessage fillDateProcess(String callback, Employee currentEmployee, SendMessage sendMessage) {
        // ДОБАВЛЯЕМ ДАННЫЕ В БУФЕР
        postgres.addValueInBufferFromPrinter(currentEmployee, DateService.parseStringToSqlDate(callback), "date");
        // ПЕРЕНОСИМ ДАННЫЕ СТАТИСТИКИ ИЗ БУФЕРА В ТАБЛИЦУ
        PrinterStatistic addedStat = postgres.moveDataFromPrinterBufferToMainTable(currentEmployee);
        // ЕСЛИ УСПЕШНО, ВОЗВРАЩАЕМ ОТВЕТ И АСИНХРОННО ОБНОВЛЯЕМ GOOGLE ТАБЛИЦУ
        if (addedStat != null) {
            currentEmployee.setStatus(EmployeeStatus.SAVED);
            employeeService.save(currentEmployee);
            sendMessage.setText(String.format("Я все записал!\n%s", postgres.getNicePhraseToPrinter(currentEmployee)));
            CompletableFuture.runAsync(()->statisticHandler.processPrinterStatistic(addedStat))
                    .exceptionally(exception->{
                        log.error("ПРОИЗОШЛА ОШИБКА ВО ВРЕМЯ ДОБАВЛЕНИЯ СТАТИСТИКИ ПЕЧАТНИКА В GOOGLE ТАБЛИЦУ - {}", exception.getMessage());
                        return null;
                    });
        }else{
            sendMessage.setText("У меня не очень получилось записать :(\nМожет, попробовать еще раз?");
        }
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getShowAndAddKeyboard());
        return sendMessage;
    }

    /**
     * Добавляет нового печатника в базу и обновляет google таблицу
     * @param sendMessage сообщение SendMessage
     * @param currentEmployee пользователь
     * @param workTime enum класса DayNight
     * @return сообщение SendMessage
     */
    private SendMessage addNewPrinter(SendMessage sendMessage, Employee currentEmployee, DayNight workTime) {
        currentEmployee.setWorkTime(workTime);
        currentEmployee.setJob(EmployeePost.PRINTER);
        currentEmployee.setStatus(EmployeeStatus.SAVED);
        employeeService.save(currentEmployee);
        CompletableFuture.runAsync(statisticHandler::processUpdateTable)
                .exceptionally(exception->{
                    log.error("ПРОИЗОШЛА ОШИБКА ВО ВРЕМЯ ОБНОВЛЕНИЯ ТАБЛИЦЫ GOOGLE ПРИ ДОБАВЛЕНИИ НОВОГО ПЕЧАТНИКА- {}", exception.getMessage());
                    return null;
                });
        sendMessage.setText("""
                Отлично 👍 Чтобы записать статистику,\s
                нажмите «Добавить статистику»
                """);
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getAddStatKeyboard());
        return sendMessage;
    }

    /**
     * Добавляет нового сборщика в базу и обновляет google таблицу
     * @param sendMessage сообщение SendMessage
     * @param currentEmployee пользователь
     * @return сообщение SendMessage
     */
    private SendMessage addNewPacker(SendMessage sendMessage, Employee currentEmployee) {
        currentEmployee.setJob(EmployeePost.PACKER);
        currentEmployee.setStatus(EmployeeStatus.SAVED);
        employeeService.save(currentEmployee);
        CompletableFuture.runAsync(statisticHandler::processUpdateTable)
                .exceptionally(exception->{
                    log.error("ПРОИЗОШЛА ОШИБКА ВО ВРЕМЯ ОБНОВЛЕНИЯ ТАБЛИЦЫ GOOGLE ПРИ ДОБАВЛЕНИИ НОВОГО СБОРЩИКА - {}", exception.getMessage());
                    return null;
                });
        sendMessage.setText("""
                Отлично 👍 Чтобы записать статистику,\s
                нажмите «Добавить новую статистику»
                """);
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getAddStatKeyboard());
        return sendMessage;
    }


}
