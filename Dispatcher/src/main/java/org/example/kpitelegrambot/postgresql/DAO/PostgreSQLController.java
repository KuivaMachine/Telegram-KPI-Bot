package org.example.kpitelegrambot.postgresql.DAO;

import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.postgresql.data.Months;
import org.example.kpitelegrambot.postgresql.entity.*;
import org.example.kpitelegrambot.postgresql.service.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;

@Log4j2
@Controller
public class PostgreSQLController {


    private final JdbcTemplate jdbcTemplate;


    @Autowired
    public PostgreSQLController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
makeSqlRequestByStatement("CREATE TABLE IF NOT EXISTS nice_words (id SERIAL PRIMARY KEY NOT NULL, phrase CHARACTER (255));");
makeSqlRequestByStatement("INSERT INTO nice_words (phrase) VALUES ('Эта статистика – просто снос башки! \uD83E\uDD2F'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы – звезда офиса! \uD83D\uDE0E'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Крутотень, продолжайте в том же духе! \uD83D\uDCAA'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вау, ваши результаты - просто огонь!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы - супергерой нашего проекта! ❤\uFE0F'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваш подход к работе – топчик! ☺'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваши навыки на высоте!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы - сладкая конфетка)) \uD83C\uDF6D'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы - профессионал! \uD83D\uDE0E'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваша продуктивность зашкаливает! \uD83E\uDD73'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы внесли огромный вклад в общий успех!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваша работа – это чистый гений! \uD83E\uDD17'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы подняли планку на новый уровень! \uD83D\uDE0E'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваша энергия заряжает всю команду! \uD83E\uDD73'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы сделали всё на максималках! \uD83D\uDCAA'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы - лучший сотрудник в офисе)) \uD83D\uDCAA'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваша статистика войдет в историю MustHaveCase! \uD83D\uDE09'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы - просто космос! \uD83C\uDF0C'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы стали легендой среди коллег! \uD83C\uDFC6'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы двигаете компанию вперёд!'); "  +
        "INSERT INTO nice_words (phrase) VALUES ('Вы достойны аплодисментов! \uD83D\uDC4F'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваша работа – эталон качества! ❤\uFE0F'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы - настоящий талант нашего времени! \uD83E\uDD17'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы превзошли все ожидания! \uD83D\uDE31'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы абсолютный чемпион в своём деле! \uD83D\uDE0E'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы заставляете меня гордиться вами! ☺'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы продемонстрировали свои таланты на полную!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы умеете справляться с любыми вызовами!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы готовы к новым вершинам! \uD83D\uDE0E'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы оставили свой след в истории компании!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы задаёте тренды в работе!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы - абсолютный чемпион в своём деле! \uD83D\uDE0E'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы становитесь лучше с каждым днём!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы прокладываете путь к успеху! \uD83D\uDCAA'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы превращаете мечты в реальность!\uD83C\uDF08\uD83E\uDD84'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы поднимаете настроение всей команде! ❤\uFE0F'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы уже стали частью истории успеха!\uD83C\uDF0C✨'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы - бесподобны! \uD83C\uDFF0\uD83D\uDC78'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Горжусь Вами)) \uD83D\uDE0A'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы меняете мир к лучшему! \uD83E\uDD17'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Отличная работа! ☺'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Браво! Это было невероятно! \uD83D\uDCA5'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Спасибо за ваше упорство и трудолюбие!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Работать с вами - сплошное удовольствие)) \uD83C\uDF81❤\uFE0F'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вы проделали фантастическую работу!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Так держать! \uD83D\uDCAA'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Отлично справились! Ваше стремление к совершенству вдохновляет!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваша работа заслуживает самой высокой оценки!'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Потрясающий результат! Вы - настоящий ас! \uD83D\uDE0E'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Это была блестящая работа! Вы - настоящая звезда! \uD83C\uDF1F'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ого, вы превзошли самого себя! \uD83E\uDD2F'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваш вклад в проект неоценим! ❤\uFE0F'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Работа выполнена идеально! ☺'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Вам стоит гордиться собой! Отличная работа! \uD83D\uDE0A'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Результат говорит сам за себя! Вы справились на высшем уровне!! \uD83D\uDE0D'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Так держать! Вы двигаетесь в правильном направлении! \uD83D\uDE07'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Невероятно! Вы сделали невозможное возможным! \uD83D\uDE2E'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Я потрясен! Отличная работа! \uD83E\uDD29'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Ваш босс гордится Вами! Только \uD83E\uDD2B'); " +
        "INSERT INTO nice_words (phrase) VALUES ('Бесподобно! С такой статистикой повышение не за горами) \uD83D\uDE09'); ");
makeSqlRequestByStatement("CREATE TABLE IF NOT EXISTS motivation_words (id SERIAL PRIMARY KEY NOT NULL, phrase CHARACTER (255));");
makeSqlRequestByStatement("INSERT INTO motivation_words (phrase) VALUES ('Знаю, ты можешь лучше) \uD83D\uDE09'); " +
        "INSERT INTO motivation_words (phrase) VALUES ('Маленькие шаги ведут к большим победам! \uD83D\uDDFA\uFE0F'); " +
        "INSERT INTO motivation_words (phrase) VALUES ('Каждая ошибка — это урок на пути к успеху! \uD83D\uDCDA'); " +
        "INSERT INTO motivation_words (phrase) VALUES ('Помни, каждый день — шанс стать лучше! \uD83D\uDD04'); " +
        "INSERT INTO motivation_words (phrase) VALUES ('Усердие и терпение приведут к результатам! \uD83D\uDCBC'); " +
        "INSERT INTO motivation_words (phrase) VALUES ('Стремись к совершенству, и успех придет! \uD83C\uDFC5'); " +
        "INSERT INTO motivation_words (phrase) VALUES ('Каждое усилие приближает тебя к цели! \uD83C\uDFAF'); " +
        "INSERT INTO motivation_words (phrase) VALUES ('Работай над собой, и результаты не заставят себя ждать! \uD83D\uDD70\uFE0F'); " +
        "INSERT INTO motivation_words (phrase) VALUES ('Неплохо! Но можно еще лучше \uD83D\uDC4D'); " +
        "INSERT INTO motivation_words (phrase) VALUES ('Вы молодец)');");
    }

    private boolean makeSqlRequestByStatement(String sql) {
        try {
            jdbcTemplate.execute(sql);
            return true;
        } catch (DataAccessException e) {
            log.error("НЕ УДАЛОСЬ ВЫПОЛНИТЬ ЗАПРОС - {}", e.getMessage());
            return false;
        }
    }

   private void makeSqlRequestByPreparedStatement(String sql, Object data) {
        jdbcTemplate.update(sql, data);
    }

    private List<String> makeSelectRequest(String sql) {
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public void createNewPrinterStatisticTableIfNotExists(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY, date DATE UNIQUE, prints_num INT DEFAULT 0, defects_num INT DEFAULT 0, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP);", tableName);
        makeSqlRequestByStatement(sql);
    }

    public void createNewPrinterStatisticBuffer(Employee currentEmployee) {
        String tableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY, date DATE UNIQUE, prints_num INT DEFAULT 0, defects_num INT DEFAULT 0);", tableName);
        makeSqlRequestByStatement(sql);
    }


    public void addValueInBufferFromPrinter(Employee currentEmployee, Object value, String columnName) {
        String tableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sql = String.format("INSERT INTO %s (id, %s) VALUES (1, ?) ON CONFLICT (id) DO UPDATE SET %s = EXCLUDED.%s;", tableName, columnName, columnName, columnName);
        makeSqlRequestByPreparedStatement(sql, value);
    }

    public PrinterStatistic moveDataFromPrinterBufferToMainTable(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String mainTableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String insertQuery = String.format("INSERT INTO %s (date, prints_num, defects_num, created_at) SELECT date, prints_num, defects_num, CURRENT_TIMESTAMP FROM %s ON CONFLICT (date) DO UPDATE SET prints_num = EXCLUDED.prints_num, defects_num = EXCLUDED.defects_num, created_at = CURRENT_TIMESTAMP RETURNING *;", mainTableName, bufferTableName);
        PrinterStatistic stat = jdbcTemplate.queryForObject(insertQuery, new PrinterStatisticMapper());
        if (stat != null) {
            stat.setFio(currentEmployee.getFio());
        }
        deletePrinterBuffer(currentEmployee);
        return stat;
    }

    public String getLastAddedPackerRecordToString() {
        String tableName = "statistics_by_packers";
        String getLastStatRequest = String.format("SELECT * FROM %s ORDER BY created_at DESC LIMIT 1;", tableName);

        StringBuilder sb;
        try {
            PackerStatistic statistic = jdbcTemplate.queryForObject(getLastStatRequest, new PackerStatisticMapper());
            sb = new StringBuilder();
            if (statistic != null) {
                sb.append(DateService.parseSqlDateToString(statistic.getDate()))
                        .append("\n")
                        .append("WB основной: ").append(statistic.getWb_mhc())
                        .append("\n")
                        .append("ЕБ: ").append(statistic.getWb_signum())
                        .append("\n")
                        .append("СЛ: ").append(statistic.getWb_silicosha())
                        .append("\n")
                        .append("Озон: ").append(statistic.getOzon())
                        .append("\n")
                        .append("Яндекс: ").append(statistic.getYandex())
                        .append("\n")
                        .append("WB Print Kid: ").append(statistic.getWb_printkid())
                        .append("\n")
                        .append("ФБО: ").append(statistic.getFbo());
                return sb.toString();
            }
        } catch (DataAccessException e) {
            log.info("ЗАПРОС {} ВЕРНУЛ ПУСТОЙ РЕЗУЛЬТАТ - {}", getLastStatRequest, e.getMessage());
        }
        return null;
    }

    public String getLastAddedPrinterRecordToString(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String getLastStatRequest = String.format("SELECT date, prints_num, defects_num FROM %s ORDER BY created_at DESC LIMIT 1;", tableName);
        StringBuilder sb = new StringBuilder();
        sb.append(currentEmployee.getFio()).append("\n");
        try {
            PrinterStatistic statistic = jdbcTemplate.queryForObject(getLastStatRequest, new PrinterStatisticMapper());
            if (statistic != null) {
                sb.append(DateService.parseSqlDateToString(statistic.getDate()))
                        .append("\n")
                        .append("Напечатано: ").append(statistic.getPrints_num())
                        .append("\n")
                        .append("Брак: ").append(statistic.getDefects_num());
                return sb.toString();
            }
        } catch (DataAccessException e) {
            log.info("ЗАПРОС {} ВЕРНУЛ ПУСТОЙ РЕЗУЛЬТАТ - {}", getLastStatRequest, e.getMessage());
        }
        return null;
    }

    public void deletePrinterBuffer(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sqlDropRequest = String.format("DROP TABLE IF EXISTS %s;", bufferTableName);
        makeSqlRequestByStatement(sqlDropRequest);
    }

    public String getNicePhraseToPrinter(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sqlGetRequest = String.format("SELECT (prints_num) FROM %s WHERE id = 1;", bufferTableName);
        String nicePhrase;

        List<String> result = makeSelectRequest(sqlGetRequest);

        int printsNum = Integer.parseInt(result.getFirst());
        if (printsNum > 100) {
            nicePhrase = getNicePhrase();
        } else {
            nicePhrase = getMotivationPhrase();
        }

        return nicePhrase;
    }

    private String getMotivationPhrase() {
        String motivationPhrase;
        Random random = new Random();
        int randInt = random.nextInt(10) + 1;
        String sqlGetRequest = String.format("SELECT (phrase) FROM motivation_words WHERE id = %d;", randInt);
        List<String> result = makeSelectRequest(sqlGetRequest);

        motivationPhrase = result.getFirst();

        return motivationPhrase;
    }

    public String getNicePhrase() {
        String nicePhrase;
        Random random = new Random();
        int randInt = random.nextInt(60) + 1;
        String sqlGetRequest = String.format("SELECT (phrase) FROM nice_words WHERE id = %d;", randInt);
        List<String> result = makeSelectRequest(sqlGetRequest);
        nicePhrase = result.getFirst();
        return nicePhrase;
    }

    public void createNewPackerStatisticTableIfNotExists() {
        String tableName = "statistics_by_packers";
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (date DATE PRIMARY KEY NOT NULL, wb_mhc INT DEFAULT 0, wb_signum INT DEFAULT 0, wb_silicosha INT DEFAULT 0, ozon INT DEFAULT 0, yandex INT DEFAULT 0, wb_printKid INT DEFAULT 0, fbo INT DEFAULT 0, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP);", tableName);
        makeSqlRequestByStatement(sql);
    }

    public void createNewPackerStatisticBuffer(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY NOT NULL, date DATE, wb_mhc INT DEFAULT 0, wb_signum INT DEFAULT 0, wb_silicosha INT DEFAULT 0, ozon INT DEFAULT 0, yandex INT DEFAULT 0, wb_printKid INT DEFAULT 0, fbo INT DEFAULT 0);", bufferTableName);
        makeSqlRequestByStatement(sql);
    }

    public void addValueInBufferFromPacker(Employee currentEmployee, Object value, String columnName) {
        String tableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String sql = String.format("INSERT INTO %s (id, %s) VALUES (1, ?) ON CONFLICT (id) DO UPDATE SET %s = EXCLUDED.%s;", tableName, columnName, columnName, columnName);
        makeSqlRequestByPreparedStatement(sql, value);
    }

    public void deletePackerBuffer(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String sqlDropRequest = String.format("DROP TABLE IF EXISTS %s;", bufferTableName);
        makeSqlRequestByStatement(sqlDropRequest);
    }

    public PackerStatistic moveDataFromPackerBufferToMainTable(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String mainTableName = "statistics_by_packers";
        String sql = String.format("INSERT INTO %s (date, wb_mhc, wb_signum, wb_silicosha, ozon, yandex, wb_printKid, fbo, created_at) SELECT date,wb_mhc,wb_signum,wb_silicosha,ozon,yandex,wb_printKid,fbo, CURRENT_TIMESTAMP FROM %s ON CONFLICT (date) DO UPDATE SET date = excluded.date,wb_mhc= excluded.wb_mhc,wb_signum= excluded.wb_signum,wb_silicosha = excluded.wb_silicosha,ozon= excluded.ozon,yandex= excluded.yandex,wb_printkid= excluded.wb_printkid,fbo= excluded.fbo,created_at=CURRENT_TIMESTAMP RETURNING*;", mainTableName, bufferTableName);
        PackerStatistic statistic = jdbcTemplate.queryForObject(sql, new PackerStatisticMapper());
        deletePackerBuffer(currentEmployee);
        return statistic;
    }



    // Вспомогательный метод для проверки существования таблицы
    private boolean doesTableExist(String tableName) {
        String doesTableExistRequest = String.format("SELECT 1 FROM information_schema.tables WHERE table_name = '%s'", tableName);
        return makeSelectRequest(doesTableExistRequest).equals(List.of("1"));
    }

    // Вспомогательный метод для получения первого и последнего дня месяца
    public List<LocalDate> getFirstAndLastDayOfMonth(String table) {
        int year = Integer.parseInt(table.substring(table.length() - 4));
        int month = 0;
        for (Months m : Months.values()) {
            if (table.toLowerCase().contains(m.getTranslation())) {
                month = m.getNumber();
                break;
            }
        }
        LocalDate firstDayOfMonth = YearMonth.of(year, month).atDay(1);
        LocalDate lastDayOfMonth = YearMonth.of(year, month).atEndOfMonth();
        return List.of(firstDayOfMonth, lastDayOfMonth);
    }

    // Метод для выполнения выборки статистики
    private <T> List<T> executeQuery(String query, RowMapper<T> rowMapper) {
        log.info("ДЛЯ ОБНОВЛЕНИЯ ТАБЛИЦЫ БЫЛА СДЕЛАНА ВЫБОРКА {}", query);
        return jdbcTemplate.query(query, rowMapper);
    }


    public List<PrinterStatistic> getPrinterStatisticByChatId(long chatId, String table) {
        String tableName = String.format("statistic_from_%s", chatId);
        if (doesTableExist(tableName)) {
            List<LocalDate> dates = getFirstAndLastDayOfMonth(table);
            String selectStatRequest = String.format("SELECT * FROM %s WHERE date >= '%s' AND date <= '%s';",
                    tableName, dates.getFirst(), dates.getLast());
            return executeQuery(selectStatRequest, new PrinterStatisticMapper());
        } else {
            log.info(String.format("ТАБЛИЦЫ ПЕЧАТНИКА %s НЕ СУЩЕСТВУЕТ", chatId));
            return null;
        }
    }


    public List<PackerStatistic> getAllPackerStatistics(String table) {
        String tableName = "statistics_by_packers";
        if (doesTableExist(tableName)) {
            List<LocalDate> dates = getFirstAndLastDayOfMonth(table);
            String selectStatRequest = String.format("SELECT * FROM %s WHERE date >= '%s' AND date <= '%s';",
                    tableName, dates.getFirst(), dates.getLast());
            return executeQuery(selectStatRequest, new PackerStatisticMapper());
        } else {
            log.info("ТАБЛИЦЫ СБОРЩИКОВ НЕ СУЩЕСТВУЕТ");
            return null;
        }
    }

    public boolean deleteLastPackerRecord() {
        String deleteRequest = "DELETE FROM statistics_by_packers WHERE date = (SELECT date FROM statistics_by_packers ORDER BY created_at DESC LIMIT 1);";
        return makeSqlRequestByStatement(deleteRequest);
    }
    public boolean deleteLastPrinterRecord(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String deleteRequest = String.format("DELETE FROM %s WHERE date = (SELECT date FROM %s ORDER BY created_at DESC LIMIT 1);", tableName, tableName);
        return makeSqlRequestByStatement(deleteRequest);
    }

}