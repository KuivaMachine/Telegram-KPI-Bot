package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.kpitelegrambot.bot.keyboards.InlineKeyboardFactory;
import org.example.kpitelegrambot.data.AnswersList;
import org.example.kpitelegrambot.googlesheets.KafkaProducer;
import org.example.kpitelegrambot.postgresql.data.EmployeePost;
import org.example.kpitelegrambot.postgresql.data.EmployeeStatus;
import org.example.kpitelegrambot.postgresql.entity.Employee;
import org.example.kpitelegrambot.postgresql.service.EmployeeService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;


@Component
@RequiredArgsConstructor
public class UpdateHandler implements Handler {

    private final EmployeeService employeeService;
    Employee employee;
    TelegramBot telegramBot;
    private final PrinterHandler printerHandler;
    private final PackerHandler packerHandler;
    private final KafkaProducer kafkaProducer;

    public void register(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public SendMessage process(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        employee = employeeService.getEmployeeByChatId(chatId);
        EmployeePost job = employee.getJob();

        if (text.equals("/forget_me")) {
            employeeService.deleteEmployeeByChatId(chatId);
            return forgetEmployeeProcess(sendMessage);
        }
        if (text.equals("/kuiva_machine")) {
           sendMessage.setText(AnswersList.ADMIN_COMMANDS.getText());
           sendMessage.setParseMode("HTML");
            return sendMessage;
        }
        if (text.equals("/update_table")) {
            sendMessage.setText("Обновляю таблицу KPI за текущий месяц");
            kafkaProducer.send("commands", "UPDATE");
            return sendMessage;
        }
        if (text.equals("/help")) {
            switch (job) {
                case PRINTER -> sendMessage.setText(AnswersList.HELP_MESSAGE_PRINTER.getText());
                case PACKER -> sendMessage.setText(AnswersList.HELP_MESSAGE_PACKER.getText());
                case UNKNOWN -> sendMessage.setText(AnswersList.HELP_MESSAGE_UNKNOWN.getText());
            }
            sendMessage.setParseMode("HTML");
            return sendMessage;
        }

        return (switch (job) {
            case PACKER -> packerHandler.process(telegramBot, update, employee, sendMessage);
            case PRINTER -> printerHandler.process(telegramBot, update, employee, sendMessage);
            case UNKNOWN -> registrationProcess(sendMessage, employee, update);
        });

    }

    private SendMessage forgetEmployeeProcess(SendMessage sendMessage) {
        sendMessage.setText(AnswersList.FORGET_ME.getText());
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }


    private SendMessage registrationProcess(SendMessage sendMessage, Employee employee, Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        if (employee.getStatus().equals(EmployeeStatus.UNKNOWN_USER)) {
            return newUserProcessing(sendMessage, employee, update);
        }
        if (employee.getStatus().equals(EmployeeStatus.WAITING_FIO)) {
            return fillFioProcessing(sendMessage, employee, text);
        }
        if (employee.getStatus().equals(EmployeeStatus.WAITING_JOB)) {
            telegramBot.deleteLastMessage(chatId, update.getMessage().getMessageId());
            sendMessage.setText(AnswersList.INVALID_JOB.getText());
            sendMessage.setReplyMarkup(InlineKeyboardFactory.getJobChoiceKeyboard());
        }
        if (employee.getStatus().equals(EmployeeStatus.WAITING_WORKTIME)) {
            telegramBot.deleteLastMessage(chatId, update.getMessage().getMessageId());
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
        return sendMessage;
    }

}
