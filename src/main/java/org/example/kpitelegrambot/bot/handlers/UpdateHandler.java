package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.kpitelegrambot.bot.keyboards.InlineKeyboardFactory;
import org.example.kpitelegrambot.data.EmployeeStatus;
import org.example.kpitelegrambot.DAO.entity.Employee;
import org.example.kpitelegrambot.service.EmployeeService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
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

    public void register(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public SendMessage process(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Я не знаю такой команды \uD83E\uDD37");
        employee = employeeService.getEmployeeByChatId(chatId);

        if (text.equals("/forget_me")) {
            employeeService.deleteEmployeeByChatId(chatId);
            return forgetEmployeeProcess(sendMessage);
        }
        return (switch (employee.getJob()) {
            case PACKER -> packerHandler.process(telegramBot, update, employee, sendMessage);
            case PRINTER -> printerHandler.process(telegramBot,update, employee, sendMessage);
            case UNKNOWN -> registrationProcess(sendMessage, employee, update);
        });

    }

    private SendMessage forgetEmployeeProcess(SendMessage sendMessage) {
        sendMessage.setText("""
                Начнем с начала)
                Например, с команды /start
                """);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
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
            sendMessage.setText("""
                    Ну так кем Вы работаете?)
                    """);
            sendMessage.setReplyMarkup(InlineKeyboardFactory.getJobChoiceKeyboard());
        }
        if (employee.getStatus().equals(EmployeeStatus.WAITING_WORKTIME)) {
            telegramBot.deleteLastMessage(chatId, update.getMessage().getMessageId());
            sendMessage.setText("""
                Вы работаете дневную или в ночную смену?)
                """);
            sendMessage.setReplyMarkup(InlineKeyboardFactory.getDayNightKeyboard());
        }

        return sendMessage;
    }


    private SendMessage fillFioProcessing(SendMessage sendMessage, Employee employee, String text) {
        if (matchesFio(text)) {
            employee.setFio(text);
            employee.setStatus(EmployeeStatus.WAITING_JOB);
            employeeService.save(employee);
            sendMessage.setText("""
                    Приятно познакомиться ☺
                    А кем Вы работаете?)
                    """);
            sendMessage.setReplyMarkup(InlineKeyboardFactory.getJobChoiceKeyboard());
        } else {
            sendMessage.setText(String.format("Не совсем верно \uD83D\uDE42\nВаше ФИО, каждое слово с большой буквы, через пробел ☝\nФормат: «Фамилия Имя Отчество»\n\nВы ввели: \"%s\"", text));
        }
        return sendMessage;
    }

    private boolean matchesFio(String text) {
        String regex = ".*\\d.*";
        String[] words = text.split(" ");
        if(words.length > 3){
            return false;
        }
        int n = 0;
        for (String word : words) {
            if(!word.isEmpty()){
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
        sendMessage.setText("""
                Привет! 👋 Я бот KPI, я буду записывать Вашу статистику!  \uD83D\uDE0E
                Введите Ваше ФИО, я его запомню.\s
                Например: «Никифорова Екатерина Лемаровна» \uD83D\uDE42""");
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        return sendMessage;
    }

}
