package org.example.kpitelegrambot.bot.handlers;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.bot.keyboards.ReplyKeyboardFactory;
import org.example.kpitelegrambot.bot.enums.AnswersList;
import org.example.kpitelegrambot.bot.enums.ButtonLabels;
import org.example.kpitelegrambot.googlesheets.StatisticHandler;
import org.example.kpitelegrambot.data.service.StatisticService;
import org.example.kpitelegrambot.bot.enums.EmployeeStatus;
import org.example.kpitelegrambot.data.entity.Employee;
import org.example.kpitelegrambot.data.entity.PackerStatistic;
import org.example.kpitelegrambot.data.service.DateService;
import org.example.kpitelegrambot.data.service.EmployeeService;
import org.example.kpitelegrambot.data.service.LogService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.CompletableFuture;

/**
 * Обработчик событий для сборщика
 */
@Component
@RequiredArgsConstructor
public class PackerHandler implements JobHandler {
    private final EmployeeService employeeService;
    private final StatisticService postgres;
    private final StatisticHandler statisticHandler;
    private final LogService log;


    /**
     * Основной обработчик сообщения
     * @param update обновление
     * @param currentEmployee сборщик
     * @param sendMessage сообщение
     * @return сообщение класса SendMessage
     */
    @Override
    public SendMessage process(Update update, Employee currentEmployee, SendMessage sendMessage) {
        sendMessage.setText(AnswersList.PACKER_INVALID_COMMAND.getText());
        String receivedMessage = update.getMessage().getText();

        // ЕСЛИ СБОРЩИК НАЧАЛ ПЕРЕПИСКУ С НАЧАЛА
        if (receivedMessage.equals("/start")) {
            return sayHelloProcess(sendMessage, currentEmployee);
        }
        // ЕСЛИ СБОРЩИК УДАЛЯЕТ СТАТИСТИКУ
        if (currentEmployee.getStatus().equals(EmployeeStatus.DELETING)) {
            if (receivedMessage.equals(ButtonLabels.YES.getLabel())) {
                return deleteLastRecord(currentEmployee, sendMessage);
            }
            if (receivedMessage.equals(ButtonLabels.NO.getLabel())) {
                return cancelAddingStatistic(sendMessage, currentEmployee);
            }
        }
        // ЕСЛИ СБОРЩИК НАЖАЛ "ОТМЕНИТЬ ДОБАВЛЕНИЕ"
        if (receivedMessage.equals(ButtonLabels.CANCEL_ADDING.getLabel())) {
            return cancelAddingStatistic(sendMessage, currentEmployee);
        }
        // ЕСЛИ СТАТУС СБОРЩИКА "SAVED"
        if (currentEmployee.getStatus().equals(EmployeeStatus.SAVED)) {
            // ЕСЛИ НАЖАЛ "Добавить новую статистику"
            if (receivedMessage.equals(ButtonLabels.ADD_NEW_STATISTICS.getLabel())) {
                return createNewStatisticPost(currentEmployee, sendMessage);
            }
            // ЕСЛИ НАЖАЛ "Показать последнюю запись"
            if (receivedMessage.equals(ButtonLabels.SHOW_STATISTIC.getLabel())) {
                return showLastRecord(currentEmployee, sendMessage);
            }
            // ЕСЛИ НАЖАЛ "Удалить последнюю запись"
            if (receivedMessage.equals(ButtonLabels.DELETE_LAST_RECORD.getLabel())) {
                return deletingLastRecordProcess(currentEmployee, sendMessage);
            }
        }
        // ЕСЛИ ОЖИДАЕТСЯ ЧИСЛО СОБРАННЫХ ЗАКАЗОВ ДЛЯ WB_MHC
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_WB_MHC)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillWBMHCNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        // ЕСЛИ ОЖИДАЕТСЯ ЧИСЛО СОБРАННЫХ ЗАКАЗОВ ДЛЯ SIGNUM
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_SIGNUM)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillWbSignumNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        // ЕСЛИ ОЖИДАЕТСЯ ЧИСЛО СОБРАННЫХ ЗАКАЗОВ ДЛЯ SL
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_SL)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillWbSlNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        // ЕСЛИ ОЖИДАЕТСЯ ЧИСЛО СОБРАННЫХ ЗАКАЗОВ ДЛЯ OZON
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_OZON)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillOzonNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        // ЕСЛИ ОЖИДАЕТСЯ ЧИСЛО СОБРАННЫХ ЗАКАЗОВ ДЛЯ YANDEX
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_YANDEX)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillYandexNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        // ЕСЛИ ОЖИДАЕТСЯ ЧИСЛО СОБРАННЫХ ЗАКАЗОВ ДЛЯ PRINT_KID
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_PRINT_KID)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillPrintKidNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        // ЕСЛИ ОЖИДАЕТСЯ ЧИСЛО СОБРАННЫХ ЗАКАЗОВ ДЛЯ FBO)
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_FBO)) {
            if (receivedMessage.matches("\\d{1,4}")) {
                return fillFboNumberProcess(sendMessage, currentEmployee, receivedMessage);
            } else {
                return invalidNumberProcess(sendMessage, receivedMessage);
            }
        }
        // ЕСЛИ СБОРЩИК ВВЕЛ СООБЩЕНИЕ, КОГДА ОЖИДАЕТСЯ ВЫБОР ДАТЫ
        if (currentEmployee.getStatus().equals(EmployeeStatus.WAITING_DATE)) {
            return invalidDateProcess(sendMessage);
        }
        return sendMessage;


    }

    private SendMessage deleteLastRecord(Employee currentEmployee, SendMessage sendMessage) {
        String lastAddedPackerRecord = postgres.getLastAddedPackerRecordToString();
        if (postgres.deleteLastPackerRecord()) {
            sendMessage.setText(AnswersList.DELETE_COMPLETE.getText());
            CompletableFuture.runAsync(statisticHandler::processUpdateTable)
                    .thenRun(()-> log.info(String.format("ЗАПИСЬ СБОРКИ %s УСПЕШНО УДАЛЕНА", lastAddedPackerRecord)))
                    .exceptionally(exception->{
                        log.error(String.format("ПРОИЗОШЛА ОШИБКА ВО ВРЕМЯ ОБНОВЛЕНИЯ ТАБЛИЦЫ - %s", exception.getMessage()));
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
        String lastAddedPackerRecord = postgres.getLastAddedPackerRecordToString();
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
        postgres.addValueInBufferFromPacker(currentEmployee, DateService.getLocalDate(), "date");

        PackerStatistic statistic = postgres.moveDataFromPackerBufferToMainTable(currentEmployee);
        if (statistic != null) {
            String nicePhrase = postgres.getRandomPhrase("nice_words");
            currentEmployee.setStatus(EmployeeStatus.SAVED);
            employeeService.save(currentEmployee);
            sendMessage.setText(String.format("Я все записал!\n%s", nicePhrase));
            CompletableFuture.runAsync(
                    () -> statisticHandler.processPackerStatistic(statistic)
            ).exceptionally(exception-> {
                log.error(String.format("ПРОИЗОШЛА ОШИБКА ВО ВРЕМЯ ДОБАВЛЕНИЯ СТАТИСТИКИ СБОРКИ В GOOGLE ТАБЛИЦУ - %s", exception.getMessage()));
                return null;
            });

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
        sendMessage.setReplyMarkup(ReplyKeyboardFactory.getShowAndAddKeyboard());
        return sendMessage;
    }

    public SendMessage invalidNumberProcess(SendMessage sendMessage, String text) {
        sendMessage.setText(String.format("Вы ввели %s\nКакое-то подозрительное число \uD83D\uDE11 \nПопробуйте еще разок?)", text));
        return sendMessage;
    }

    public SendMessage showLastRecord(Employee currentEmployee, SendMessage sendMessage) {
        String lastAddedPackerRecord=postgres.getLastAddedPackerRecordToString();
        if(lastAddedPackerRecord!=null){
            sendMessage.setText(lastAddedPackerRecord);
        }else{
            sendMessage.setText(AnswersList.EMPTY_RESULT.getText());
        }
        return sendMessage;
    }
}