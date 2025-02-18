package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.bot.TelegramBot;
import org.example.kpitelegrambot.bot.keyboards.ReplyKeyboardFactory;
import org.example.kpitelegrambot.data.AnswersList;
import org.example.kpitelegrambot.data.ButtonLabels;
import org.example.kpitelegrambot.googlesheets.KafkaProducer;
import org.example.kpitelegrambot.postgresql.DAO.PostgreSQLController;
import org.example.kpitelegrambot.postgresql.data.EmployeeStatus;
import org.example.kpitelegrambot.postgresql.entity.Employee;
import org.example.kpitelegrambot.postgresql.entity.PackerStatistic;
import org.example.kpitelegrambot.postgresql.service.DateService;
import org.example.kpitelegrambot.postgresql.service.EmployeeService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
@RequiredArgsConstructor
public class PackerHandler implements JobHandler {
    private final EmployeeService employeeService;
    private final PostgreSQLController postgres;
    private final DateService dateService;
    private final KafkaProducer kafkaProducer;

    @Override
    public SendMessage process(TelegramBot telegramBot, Update update, Employee currentEmployee, SendMessage sendMessage) {
        sendMessage.setText(AnswersList.PACKER_INVALID_COMMAND.getText());
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
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_WB_MHC)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillWBMHCNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_SIGNUM)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillWbSignumNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_SL)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillWbSlNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_OZON)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillOzonNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_YANDEX)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillYandexNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_PRINT_KID)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillPrintKidNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_FBO)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillFboNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_DATE)) {
            return invalidDateProcess(sendMessage);
        }
        return sendMessage;


    }

    public SendMessage sayHelloProcess(SendMessage sendMessage, Employee currentEmployee) {
        postgres.deletePackerBuffer(currentEmployee);
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

    private SendMessage fillFboNumberProcess(SendMessage sendMessage, Employee currentEmployee, String fbo) {
        postgres.addValueInBufferFromPacker(currentEmployee, Integer.parseInt(fbo), "fbo");
        postgres.addValueInBufferFromPacker(currentEmployee, dateService.getLocalDate(), "date");

        PackerStatistic statistic = postgres.moveDataFromPackerBufferToMainTable(currentEmployee);
        if (statistic != null) {
            String nicePhrase = postgres.getNicePhrase();
            currentEmployee.setStatus(EmployeeStatus.SAVED);
            employeeService.save(currentEmployee);
            sendMessage.setText(String.format("Я все записал!\n%s", nicePhrase));
            kafkaProducer.send("packer_stat_topic", statistic);
        } else {
            sendMessage.setText(AnswersList.MOVE_DATA_ERROR.getText());
        }
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getShowAndAddKeyboard());
        return sendMessage;
    }

    private SendMessage fillPrintKidNumberProcess(SendMessage sendMessage, Employee currentEmployee, String wb_printKid) {
        postgres.addValueInBufferFromPacker(currentEmployee, Integer.parseInt(wb_printKid), "wb_printKid");
        currentEmployee.setStatus(EmployeeStatus.WAITING_FBO);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.FBO_REQUEST.getText());
        return sendMessage;
    }

    private SendMessage fillYandexNumberProcess(SendMessage sendMessage, Employee currentEmployee, String yandex) {
        postgres.addValueInBufferFromPacker(currentEmployee, Integer.parseInt(yandex), "yandex");
        currentEmployee.setStatus(EmployeeStatus.WAITING_PRINT_KID);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.PRINT_KID_REQUEST.getText());
        return sendMessage;
    }

    private SendMessage fillOzonNumberProcess(SendMessage sendMessage, Employee currentEmployee, String ozon) {
        postgres.addValueInBufferFromPacker(currentEmployee, Integer.parseInt(ozon), "ozon");
        currentEmployee.setStatus(EmployeeStatus.WAITING_YANDEX);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.YANDEX_REQUEST.getText());
        return sendMessage;
    }

    private SendMessage fillWbSlNumberProcess(SendMessage sendMessage, Employee currentEmployee, String wb_silicosha) {
        postgres.addValueInBufferFromPacker(currentEmployee, Integer.parseInt(wb_silicosha), "wb_silicosha");
        currentEmployee.setStatus(EmployeeStatus.WAITING_OZON);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.OZON_REQUEST.getText());
        return sendMessage;
    }

    private SendMessage fillWbSignumNumberProcess(SendMessage sendMessage, Employee currentEmployee, String wb_signum) {
        postgres.addValueInBufferFromPacker(currentEmployee, Integer.parseInt(wb_signum), "wb_signum");
        currentEmployee.setStatus(EmployeeStatus.WAITING_SL);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.SL_REQUEST.getText());
        return sendMessage;
    }

    private SendMessage fillWBMHCNumberProcess(SendMessage sendMessage, Employee currentEmployee, String wb_mhc) {
        postgres.addValueInBufferFromPacker(currentEmployee, Integer.parseInt(wb_mhc), "wb_mhc");
        currentEmployee.setStatus(EmployeeStatus.WAITING_SIGNUM);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.SIGNUM_REQUEST.getText());
        return sendMessage;
    }


    public SendMessage createNewStatisticPost(Employee currentEmployee, SendMessage sendMessage) {
        postgres.createNewPackerStatisticTableIfNotExists();
        postgres.createNewPackerStatisticBuffer(currentEmployee);
        currentEmployee.setStatus(EmployeeStatus.WAITING_WB_MHC);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.WB_MHC_REQUEST.getText());
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getCancelKeyboard());
        return sendMessage;
    }

    public SendMessage cancelAddingStatistic(SendMessage sendMessage, Employee currentEmployee) {
        postgres.deletePackerBuffer(currentEmployee);
        currentEmployee.setStatus(EmployeeStatus.SAVED);
        employeeService.save(currentEmployee);
        sendMessage.setText(AnswersList.CANCEL_MESSAGE.getText());
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getAddStatKeyboard());
        return sendMessage;
    }

    public SendMessage invalidNumberProcess(SendMessage sendMessage, String text) {
        sendMessage.setText(String.format("Вы ввели %s \nКакое-то подозрительное число \uD83D\uDE11 \nПопробуйте еще разок?)", text));
        return sendMessage;
    }

    public SendMessage showLastRecord(Employee currentEmployee, SendMessage sendMessage) {
        sendMessage.setText(postgres.getLastAddedPackerRecordToString());
        return sendMessage;
    }
}