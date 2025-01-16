package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.DAO.PostgreSQLController;
import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.kpitelegrambot.bot.keyboards.InlineKeyboardFactory;
import org.example.kpitelegrambot.data.AnswersList;
import org.example.kpitelegrambot.data.EmployeeStatus;
import org.example.kpitelegrambot.entity.Employee;
import org.example.kpitelegrambot.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Component
@RequiredArgsConstructor
public class PrinterHandler implements JobHandler {
    private static final Logger log = LoggerFactory.getLogger(PrinterHandler.class);
    private final EmployeeService employeeService;
    private final PostgreSQLController postgres;

    @Override
    public SendMessage process(TelegramBot telegramBot, Update update, Employee employee, SendMessage sendMessage) {
        sendMessage.setText("Я не знаю такой команды \uD83E\uDD37");

        if (employee.getStatus().equals(EmployeeStatus.SAVED)) {
            if (update.getMessage().getText().equals(AnswersList.ADD_NEW_STATISTIC.getText())) {
                return createNewStatisticPost(employee, sendMessage);
            }
        }
        if (employee.getStatus().equals(EmployeeStatus.WAITING_DATE)) {
            telegramBot.deleteLastMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
        }
        if (employee.getStatus().equals(EmployeeStatus.WAITING_PRINTS_NUM)) {
            if (update.getMessage().getText().matches("\\d{1,3}")) {
                return fillPrintsNumProcess(sendMessage, employee);
            }
            return fillPrintsNumProcess(sendMessage, employee);
        }

        return sendMessage;
    }

    private SendMessage fillPrintsNumProcess(SendMessage sendMessage, Employee currentEmployee) {
        return sendMessage;
    }

    private SendMessage createNewStatisticPost(Employee currentEmployee, SendMessage sendMessage) {
        postgres.createNewStatisticTableIfNotExists(currentEmployee);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setText("Выберите дату Вашей смены \uD83D\uDCC5");
        sendMessage.setReplyMarkup(InlineKeyboardFactory.getDateChoiceKeyboard());
        currentEmployee.setStatus(EmployeeStatus.WAITING_DATE);
        employeeService.save(currentEmployee);
        return sendMessage;
    }
}
