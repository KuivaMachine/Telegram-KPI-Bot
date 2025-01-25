package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.DAO.PostgreSQLController;
import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.kpitelegrambot.bot.keyboards.InlineKeyboardFactory;
import org.example.kpitelegrambot.bot.keyboards.ReplyKeyboardFactory;
import org.example.kpitelegrambot.data.ButtonLabels;
import org.example.kpitelegrambot.data.EmployeeStatus;
import org.example.kpitelegrambot.DAO.entity.Employee;
import org.example.kpitelegrambot.service.EmployeeService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class PrinterHandler implements JobHandler {

    private final EmployeeService employeeService;
    private final PostgreSQLController postgres;


    @Override
    public SendMessage process(TelegramBot telegramBot, Update update, Employee currentEmployee, SendMessage sendMessage) {
        sendMessage.setText("Я не знаю такой команды A \uD83E\uDD37");
        String receivedMessage = update.getMessage().getText();

        if (receivedMessage.equals("/start")) {
            return sayHelloProcess(sendMessage, currentEmployee);
        }
        if(receivedMessage.equals(ButtonLabels.CANCEL_ADDING.getLabel())){
            return cancelAddingStatistic(sendMessage, currentEmployee);
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.SAVED)) {
            if (receivedMessage.equals(ButtonLabels.ADD_NEW_STATISTICS.getLabel())) {
                return createNewStatisticPost(currentEmployee, sendMessage);
            }
            if (receivedMessage.equals(ButtonLabels.SHOW_STATISTIC.getLabel())) {
                return showLastRecord(currentEmployee, sendMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_DATE)) {
            return invalidDateProcess(sendMessage);
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_PRINTS_NUM)) {
            if (receivedMessage.matches("\\d{1,3}")) {
                return fillPrintsNumProcess(sendMessage, currentEmployee, receivedMessage);
            }else{
                return invalidNumberProcess(sendMessage,receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_DEFECTS_NUM)) {
            if (receivedMessage.matches("\\d{1,3}")) {
                return fillDefectsNumProcess(sendMessage, currentEmployee, receivedMessage);
            }else {
                return invalidNumberProcess(sendMessage,receivedMessage);
            }
        }

        return sendMessage;
    }

    public SendMessage sayHelloProcess(SendMessage sendMessage, Employee currentEmployee) {
        String[] fio = currentEmployee.getFio().split(" ");
        sendMessage.setText(String.format("Привет, %s! \nДобавим новую статистику?)",fio[1]));
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getAddStatKeyboard());
        return sendMessage;
    }

    private SendMessage invalidDateProcess(SendMessage sendMessage) {
        sendMessage.setText("Просто выберите дату Вашей смены)");
        return sendMessage;
    }

    public SendMessage cancelAddingStatistic(SendMessage sendMessage, Employee currentEmployee) {
        postgres.deletePrinterBuffer(currentEmployee);
        currentEmployee.setStatus(EmployeeStatus.SAVED);
        employeeService.save(currentEmployee);
        sendMessage.setText("""
                Я все отменил 👍 Чтобы записать статистику,\s
                нажмите «Добавить новую статистику»
                """);
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getAddStatKeyboard());
        return sendMessage;
    }

    public SendMessage invalidNumberProcess(SendMessage sendMessage, String text) {
        sendMessage.setText(String.format("Вы ввели %s \nКакое-то подозрительное число \uD83D\uDE11 \nПопробуйте еще разок?", text));
        return sendMessage;
    }

    public SendMessage showLastRecord(Employee currentEmployee, SendMessage sendMessage) {
        sendMessage.setText(postgres.getLastAddedPrinterRecordToString(currentEmployee));
        return sendMessage;
    }

    private SendMessage fillDefectsNumProcess(SendMessage sendMessage, Employee currentEmployee, String numberOfDefects) {
        postgres.addValueInBufferFromPrinter(currentEmployee, Integer.parseInt(numberOfDefects), "defects_num");
        sendMessage.setText("Выберите дату Вашей смены \uD83D\uDCC5");
        sendMessage.setReplyMarkup(InlineKeyboardFactory.getDateChoiceKeyboard());
        currentEmployee.setStatus(EmployeeStatus.WAITING_DATE);
        employeeService.save(currentEmployee);
        return sendMessage;
    }

    private SendMessage fillPrintsNumProcess(SendMessage sendMessage, Employee currentEmployee, String numberOfPrints) {
        postgres.addValueInBufferFromPrinter(currentEmployee, Integer.parseInt(numberOfPrints), "prints_num");
        currentEmployee.setStatus(EmployeeStatus.WAITING_DEFECTS_NUM);
        employeeService.save(currentEmployee);
        sendMessage.setText("А сколько у Вас было брака?");
        return sendMessage;
    }

    public SendMessage createNewStatisticPost(Employee currentEmployee, SendMessage sendMessage) {
        postgres.createNewStatisticTableIfNotExists(currentEmployee);
        postgres.createNewStatisticBuffer(currentEmployee);
        currentEmployee.setStatus(EmployeeStatus.WAITING_PRINTS_NUM);
        employeeService.save(currentEmployee);
        sendMessage.setText("Сколько всего Вы напечатали?)");
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getCancelKeyboard());
        return sendMessage;
    }


}
