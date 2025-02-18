package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.kpitelegrambot.bot.keyboards.InlineKeyboardFactory;
import org.example.kpitelegrambot.bot.keyboards.ReplyKeyboardFactory;
import org.example.kpitelegrambot.data.AnswersList;
import org.example.kpitelegrambot.data.ButtonLabels;
import org.example.kpitelegrambot.postgresql.DAO.PostgreSQLController;
import org.example.kpitelegrambot.postgresql.data.EmployeeStatus;
import org.example.kpitelegrambot.postgresql.entity.Employee;
import org.example.kpitelegrambot.postgresql.service.EmployeeService;
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
        sendMessage.setText(AnswersList.PRINTER_INVALID_COMMAND.getText());
        String receivedMessage = update.getMessage().getText();
        if (receivedMessage.equals("/start")) {
            return sayHelloProcess(sendMessage, currentEmployee);
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
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_DATE)) {
            return invalidDateProcess(sendMessage);
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_PRINTS_NUM)) {
            if (receivedMessage.matches("\\d{1,3}")) {
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
      /*  //УСТАНОВИТЬ ТАЙМЕР НА 1 ЧАС, ЧТОБЫ ОТМЕНИТЬ ДОБАВЛЕНИЕ, ЕСЛИ В ТЕЧЕНИИ ЧАСА НИЧЕГО НЕ ПРОИСХОДИТ
        if (timer != null && timer.purge() > 0) { // Проверяем, был ли уже создан таймер
            stopTimer(); // Если да, то останавливаем его
        }
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                tgbot.sendAnswer(cancelAddingStatistic(sendMessage, currentEmployee)); // Вызываем метод с аргументами
                timer.cancel(); // Останавливаем таймер
            }
        };
        timer.schedule(task, 60000); // Запускаем задачу через 1 час (3600000 мс)
        log.info("ТАЙМЕР ЗАПУЩЕН НА 1 МИН");*/
        return sendMessage;
    }

   /* private void stopTimer() {
        if (timer != null) {
            timer.cancel(); // Останавливаем таймер
            timer.purge(); // Очищаем все запланированные задачи
            timer = null; // Сбрасываем ссылку на таймер
        }
    }*/

}
