package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.kpitelegrambot.bot.keyboards.InlineKeyboardFactory;
import org.example.kpitelegrambot.bot.keyboards.ReplyKeyboardFactory;
import org.example.kpitelegrambot.data.AnswersList;
import org.example.kpitelegrambot.data.ButtonLabels;
import org.example.kpitelegrambot.googlesheets.StatisticHandler;
import org.example.kpitelegrambot.postgresql.DAO.PostgreSQLController;
import org.example.kpitelegrambot.postgresql.data.EmployeeStatus;
import org.example.kpitelegrambot.postgresql.entity.Employee;
import org.example.kpitelegrambot.postgresql.service.DateService;
import org.example.kpitelegrambot.postgresql.service.EmployeeService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
@RequiredArgsConstructor
public class PrinterHandler implements JobHandler {

    private final EmployeeService employeeService;
    private final PostgreSQLController postgres;
    private final StatisticHandler statisticHandler;
    private final CallbackQueryHandler callbackQueryHandler;

    @Override
    public SendMessage process(Update update, Employee currentEmployee, SendMessage sendMessage) {
        sendMessage.setText(AnswersList.PRINTER_INVALID_COMMAND.getText());
        String receivedMessage = update.getMessage().getText();
        if (receivedMessage.equals("/start")) {
            return sayHelloProcess(sendMessage, currentEmployee);
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.DELETING)) {
            if (receivedMessage.equals(ButtonLabels.YES.getLabel())) {
                return deleteLastRecord(currentEmployee, sendMessage);
            }
            if (receivedMessage.equals(ButtonLabels.NO.getLabel())) {
                return cancelAddingStatistic(sendMessage, currentEmployee);
            }
        }
        if (receivedMessage.equals(ButtonLabels.CANCEL_ADDING.getLabel())) {
            return cancelAddingStatistic(sendMessage, currentEmployee);
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.SAVED)) {
            if (receivedMessage.equals(ButtonLabels.ADD_NEW_STATISTICS.getLabel())) {
                return createNewStatisticPost(currentEmployee, sendMessage);
            }
            if (receivedMessage.equals(ButtonLabels.SHOW_STATISTIC.getLabel())) {
                return showLastRecord(currentEmployee, sendMessage);
            }
            if (receivedMessage.equals(ButtonLabels.DELETE_LAST_RECORD.getLabel())) {
                return deletingLastRecordProcess(currentEmployee, sendMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_DATE)) {
            return invalidDateProcess(sendMessage);
        }
        if(currentEmployee.getStatus().equals(EmployeeStatus.WAITING_ANOTHER_DATE)){
            System.out.println("received another date"+receivedMessage);
            String date = receivedMessage.strip().replace(".","-");
            System.out.println("saved date"+date);
            if (date.matches("\\d{2}.-?\\d{2}.-?\\d{4}")&&DateService.isValidDate(date)) {
                return callbackQueryHandler.fillDateProcess(date, currentEmployee, sendMessage);
            }else{
                return invalidAnotherDateProcess(sendMessage, receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_PRINTS_NUM)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillPrintsNumProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_DEFECTS_NUM)) {
            if (receivedMessage.matches("\\d{1,3}")) {
                return fillDefectsNumProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }

        return sendMessage;
    }

    private SendMessage invalidAnotherDateProcess(SendMessage sendMessage, String receivedMessage) {
        sendMessage.setText(String.format("Вы ввели %s \nПопробуйте еще раз, нужна дата в формате \"дд.мм.гггг\"", receivedMessage));
        return sendMessage;
    }

    private SendMessage deleteLastRecord(Employee currentEmployee, SendMessage sendMessage) {
        if (postgres.deleteLastPrinterRecord(currentEmployee)) {
            sendMessage.setText(AnswersList.DELETE_COMPLETE.getText());
            CompletableFuture.runAsync(statisticHandler::processUpdateTable)
                    .exceptionally(exception->{
                        log.error("ПРОИЗОШЛА ОШИБКА ВО ВРЕМЯ ОБНОВЛЕНИЯ ТАБЛИЦЫ - {}", exception.getMessage());
                        return null;
                    });
        } else {
            sendMessage.setText(AnswersList.DELETE_UNCOMPLETED.getText());
        }
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getShowAndAddKeyboard());
        currentEmployee.setStatus(EmployeeStatus.SAVED);
        employeeService.save(currentEmployee);
        return sendMessage;
    }

    private SendMessage deletingLastRecordProcess(Employee currentEmployee, SendMessage sendMessage) {
        String lastAddedPackerRecord = postgres.getLastAddedPrinterRecordToString(currentEmployee);
        if(lastAddedPackerRecord!=null){
            sendMessage.setText(String.format("Последняя добавленная запись:\n%s\n\nУверены, что хотите удалить?", lastAddedPackerRecord));
            sendMessage.setReplyMarkup(ReplyKeyboardFactory.getYesNoMarkup());
            currentEmployee.setStatus(EmployeeStatus.DELETING);
        }else{
            sendMessage.setText(AnswersList.EMPTY_RESULT.getText());
            currentEmployee.setStatus(EmployeeStatus.SAVED);
        }
        employeeService.save(currentEmployee);
        return sendMessage;
    }

    public SendMessage sayHelloProcess(SendMessage sendMessage, Employee currentEmployee) {
        postgres.deletePrinterBuffer(currentEmployee);
        currentEmployee.setStatus(EmployeeStatus.SAVED);
        employeeService.save(currentEmployee);
        String[] fio = currentEmployee.getFio().split(" ");
        sendMessage.setText(String.format("Привет, %s! \nДобавим новую статистику?)", fio[1]));
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getAddStatKeyboard());
        return sendMessage;
    }

    private SendMessage invalidDateProcess(SendMessage sendMessage) {
        sendMessage.setText(AnswersList.INVALID_DATE.getText());
        return sendMessage;
    }

    public SendMessage cancelAddingStatistic(SendMessage sendMessage, Employee currentEmployee) {
        postgres.deletePrinterBuffer(currentEmployee);
        currentEmployee.setStatus(EmployeeStatus.SAVED);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.CANCEL_MESSAGE.getText());
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getShowAndAddKeyboard());
        return sendMessage;
    }

    public SendMessage invalidNumberProcess(SendMessage sendMessage, String text) {
        sendMessage.setText(String.format("Вы ввели %s \nКакое-то подозрительное число \uD83D\uDE11 \nПопробуйте еще разок?", text));
        return sendMessage;
    }

    public SendMessage showLastRecord(Employee currentEmployee, SendMessage sendMessage) {
        String lastPrinterRecord = postgres.getLastAddedPrinterRecordToString(currentEmployee);
        if(lastPrinterRecord!=null){
            sendMessage.setText(lastPrinterRecord);
        }else{
            sendMessage.setText(AnswersList.EMPTY_RESULT.getText());
        }
        return sendMessage;
    }

    private SendMessage fillDefectsNumProcess(SendMessage sendMessage, Employee currentEmployee, String numberOfDefects) {
        postgres.addValueInBufferFromPrinter(currentEmployee, Integer.parseInt(numberOfDefects), "defects_num");
        sendMessage.setText(AnswersList.DATE_CHOICE.getText());
        sendMessage.setReplyMarkup(InlineKeyboardFactory.getDateChoiceKeyboard());
        currentEmployee.setStatus(EmployeeStatus.WAITING_DATE);
        employeeService.save(currentEmployee);
        return sendMessage;
    }

    private SendMessage fillPrintsNumProcess(SendMessage sendMessage, Employee currentEmployee, String numberOfPrints) {
        postgres.addValueInBufferFromPrinter(currentEmployee, Integer.parseInt(numberOfPrints), "prints_num");
        currentEmployee.setStatus(EmployeeStatus.WAITING_DEFECTS_NUM);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.DEFECTS_NUM_REQUEST.getText());
        return sendMessage;
    }

    //ДОБАВИТЬ НОВУЮ СТАТИСТИКУ
    public SendMessage createNewStatisticPost(Employee currentEmployee, SendMessage sendMessage) {
        //СОЗДАТЬ НОВУЮ ТАБЛИЦУ(ЕСЛИ НЕ СОЗДАНА) И СОЗДАТЬ БУФЕР
        postgres.createNewPrinterStatisticTableIfNotExists(currentEmployee);
        postgres.createNewPrinterStatisticBuffer(currentEmployee);
        //СОХРАНИТЬ СТАТУС ОЖИДАНИЯ ЧИСЛА НАПЕЧАТАННОГО
        currentEmployee.setStatus(EmployeeStatus.WAITING_PRINTS_NUM);
        employeeService.save(currentEmployee);
        //ОТПРАВИТЬ СЛЕДУЮЩЕЕ СООБЩЕНИЕ И КЛАВИАТУРУ С ОТМЕНОЙ
        sendMessage.setText(AnswersList.PRINTS_NUM_REQUEST.getText());
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getCancelKeyboard());
        return sendMessage;
    }
}
