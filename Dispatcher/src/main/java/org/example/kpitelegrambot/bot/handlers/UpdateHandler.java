package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.bot.configuration.SettingsManager;
import org.example.kpitelegrambot.bot.keyboards.InlineKeyboardFactory;
import org.example.kpitelegrambot.bot.enums.AnswersList;
import org.example.kpitelegrambot.googlesheets.StatisticHandler;
import org.example.kpitelegrambot.bot.enums.EmployeePost;
import org.example.kpitelegrambot.bot.enums.EmployeeStatus;
import org.example.kpitelegrambot.data.entity.Employee;
import org.example.kpitelegrambot.data.service.EmployeeService;
import org.example.kpitelegrambot.data.service.LogService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
@RequiredArgsConstructor
public class UpdateHandler implements Handler {

    private final EmployeeService employeeService;
    private final PrinterHandler printerHandler;
    private final PackerHandler packerHandler;
    private final StatisticHandler statisticHandler;
    private final SettingsManager settingsManager;
    private final LogService log;


    @Override
    public SendMessage process(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        Employee employee = employeeService.getEmployeeByChatId(chatId);
        EmployeePost job = employee.getJob();

        switch (text) {
            case "/delete_employee"->{
                return deleteEmployeeProcess(sendMessage);
            }
            case "/forget_me" -> {
                employeeService.deleteEmployeeByChatId(chatId);
                return forgetEmployeeProcess(sendMessage);
            }
            case "/enable_admin_notification" -> {
                settingsManager.setNotificationEnabled(true);
                sendMessage.setText("Включил ежедневное оповещение");
                return sendMessage;
            }
            case "/disable_admin_notification" -> {
                settingsManager.setNotificationEnabled(false);
                sendMessage.setText("Выключил ежедневное оповещение");
                return sendMessage;
            }
            case "/kuiva_machine" -> {
                sendMessage.setText(AnswersList.ADMIN_COMMANDS.getText());
                sendMessage.setParseMode("HTML");
                return sendMessage;
            }
            case "/update_table" -> {
                sendMessage.setText("Обновляю таблицу KPI за текущий месяц");
                CompletableFuture.runAsync(statisticHandler::processUpdateTable)
                        .exceptionally(exception->{
                            log.error(String.format("ПРОИЗОШЛА ОШИБКА ВО ВРЕМЯ ОБНОВЛЕНИЯ ТАБЛИЦЫ - %s", exception.getMessage()));
                            return null;
                        });
                return sendMessage;
            }
            case "/help" -> {
                switch (job) {
                    case PRINTER -> sendMessage.setText(AnswersList.HELP_MESSAGE_PRINTER.getText());
                    case PACKER -> sendMessage.setText(AnswersList.HELP_MESSAGE_PACKER.getText());
                    case UNKNOWN -> sendMessage.setText(AnswersList.HELP_MESSAGE_UNKNOWN.getText());
                }
                sendMessage.setParseMode("HTML");
                return sendMessage;
            }
        }
        // ЕСЛИ В ГРАФЕ "УВОЛЕН" ЕСТЬ ДАТА
        if (employee.getFired()!=null){
            sendMessage.setText("Извините, после увольнения доступ к функционалу запрещен \uD83D\uDE14");
            return sendMessage;
        }

        return (switch (job) {
            case PACKER -> packerHandler.process(update, employee, sendMessage);
            case PRINTER -> printerHandler.process(update, employee, sendMessage);
            case UNKNOWN -> registrationProcess(sendMessage, employee, update);
        });

    }

    private SendMessage deleteEmployeeProcess(SendMessage sendMessage) {

        List<Employee> employees = employeeService.getEmployees();
        if (!employees.isEmpty()) {
            sendMessage.setText("Выберите работника для удаления \uD83D\uDC47");
            sendMessage.setReplyMarkup(InlineKeyboardFactory.getEmployeeListKeyboard(employees));
        }else{
            sendMessage.setText("Список активных сотрудников пуст");
        }
        return sendMessage;
    }

    private SendMessage forgetEmployeeProcess(SendMessage sendMessage) {
        sendMessage.setText(AnswersList.FORGET_ME.getText());
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }


    private SendMessage registrationProcess(SendMessage sendMessage, Employee employee, Update update) {
        String text = update.getMessage().getText();
        if (employee.getStatus().equals(EmployeeStatus.UNKNOWN_USER)) {
            return newUserProcessing(sendMessage, employee, update);
        }
        if (employee.getStatus().equals(EmployeeStatus.WAITING_FIO)) {
            return fillFioProcessing(sendMessage, employee, text);
        }
        if (employee.getStatus().equals(EmployeeStatus.WAITING_JOB)) {
            sendMessage.setText(AnswersList.INVALID_JOB.getText());
            sendMessage.setReplyMarkup(InlineKeyboardFactory.getJobChoiceKeyboard());
        }
        if (employee.getStatus().equals(EmployeeStatus.WAITING_WORKTIME)) {
            sendMessage.setText(AnswersList.WORKTIME_CHOICE.getText());
            sendMessage.setReplyMarkup(InlineKeyboardFactory.getDayNightKeyboard());
        }

        return sendMessage;
    }


    private SendMessage fillFioProcessing(SendMessage sendMessage, Employee employee, String text) {
        if (matchesFio(text)) {
            employee.setFio(text);
            employee.setStatus(EmployeeStatus.WAITING_JOB);
            employeeService.save(employee);
            sendMessage.setText(AnswersList.JOB_CHOICE.getText());
            sendMessage.setReplyMarkup(InlineKeyboardFactory.getJobChoiceKeyboard());
        } else {
            sendMessage.setText(String.format("Не совсем верно \uD83D\uDE42\nВаше ФИО, каждое слово с большой буквы, через пробел ☝\nФормат: «Фамилия Имя Отчество»\n\nВы ввели: \"%s\"", text));
        }
        return sendMessage;
    }

    private boolean matchesFio(String text) {
        String regex = ".*\\d.*";
        String[] words = text.split(" ");
        if (words.length > 3) {
            return false;
        }
        int n = 0;
        for (String word : words) {
            if (!word.isEmpty()) {
                if (Character.isUpperCase(word.charAt(0))) {
                    if (!word.matches(regex)) {
                        n++;
                    }
                }
            }
        }
        return n == 3;
    }


    private SendMessage newUserProcessing(SendMessage sendMessage, Employee employee, Update update) {
        employee.setChatId(update.getMessage().getChatId());
        employee.setUsername(update.getMessage().getFrom().getUserName());
        employee.setStatus(EmployeeStatus.WAITING_FIO);
        employeeService.save(employee);
        sendMessage.setText(AnswersList.NEW_USER_MESSAGE.getText());
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        log.info(String.format("ЗАПУЩЕН ПРОЦЕСС ДОБАВЛЕНИЯ СОТРУДНИКА, username: '%s', chat_id: %s", employee.getUsername(), employee.getChatId()));
        return sendMessage;
    }

}
