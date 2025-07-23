package org.example.kpitelegrambot.postgresql.DAO;

import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.postgresql.data.Months;
import org.example.kpitelegrambot.postgresql.entity.*;
import org.example.kpitelegrambot.postgresql.service.DateService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Класс для запросов в БД PostgreSQL
 */
@Log4j2
@Controller
public class PostgreSQLController {


    private final JdbcTemplate jdbcTemplate;


    /**
     * В конструкторе выполняется связывание бина jdbcTemplate, а также создание и заполнение таблиц nice_words и motivation_words, если таковых нет или они пустые.
     * @param jdbcTemplate
     */
    public PostgreSQLController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        makeSqlRequestByStatement("CREATE TABLE IF NOT EXISTS nice_words (id SERIAL PRIMARY KEY NOT NULL, phrase CHARACTER (255));");
        Integer nice_words_count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nice_words", Integer.class);
        if (nice_words_count != null && nice_words_count==0){
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
        }

        makeSqlRequestByStatement("CREATE TABLE IF NOT EXISTS motivation_words (id SERIAL PRIMARY KEY NOT NULL, phrase CHARACTER (255));");
        Integer motivation_words_count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM motivation_words", Integer.class);
        if (motivation_words_count != null && motivation_words_count==0){
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
    }

    /**
     * Утилитный метод по отправке простого SQL запроса
     * @param sql запрос типа String
     * @return true, если успешно, иначе false
     */
    private boolean makeSqlRequestByStatement(String sql) {
        try {
            jdbcTemplate.execute(sql);
            return true;
        } catch (DataAccessException e) {
            log.error("НЕ УДАЛОСЬ ВЫПОЛНИТЬ ЗАПРОС - {}", e.getMessage());
            return false;
        }
    }
    /**
     * Утилитный метод по отправке простого SELECT запроса
     * @param sql запрос типа String
     * @return список объектов типа String
     */
    private List<String> makeSelectRequest(String sql) {
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * Создает таблицу статистики для печатника
     * @param currentEmployee печатник типа Employee
     */
    public void createNewPrinterStatisticTableIfNotExists(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY, date DATE UNIQUE, prints_num INT DEFAULT 0, defects_num INT DEFAULT 0, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP);", tableName);
        makeSqlRequestByStatement(sql);
    }

    /**
     * Создает временную таблицу статистики для печатника
     * @param currentEmployee печатник типа Employee
     */
    public void createNewPrinterStatisticBuffer(Employee currentEmployee) {
        String tableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY, date DATE UNIQUE, prints_num INT DEFAULT 0, defects_num INT DEFAULT 0);", tableName);
        makeSqlRequestByStatement(sql);
    }


    /**
     * Добавляет значение в буфер печатника
     * @param currentEmployee печатник
     * @param value значение
     * @param columnName колонка
     */
    public void addValueInBufferFromPrinter(Employee currentEmployee, Object value, String columnName) {
        String tableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sql = String.format("INSERT INTO %s (id, %s) VALUES (1, ?) ON CONFLICT (id) DO UPDATE SET %s = EXCLUDED.%s;", tableName, columnName, columnName, columnName);
        jdbcTemplate.update(sql, value);
    }

    /**
     * Переносит данные из буфера в основную таблицу печатника, удаляет буфер
     * @param currentEmployee печатник
     * @return возвращает добавленную статистику
     */
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

    /**
     * Возвращает последнюю добавленную статистику сборки
     * @return статистику в формате:<br>
     * dd.ММ.yyyy<br>
     * WB основной: ЧИСЛО<br>
     * ЕБ: ЧИСЛО<br>
     * СЛ: ЧИСЛО<br>
     * Озон: ЧИСЛО<br>
     * Яндекс: ЧИСЛО<br>
     * WB Print Kid: ЧИСЛО<br>
     * ФБО: ЧИСЛО
     */
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
            log.info("СТАТИСТИКИ СБОРКИ НЕТ. ЗАПРОС {} ВЕРНУЛ ПУСТОЙ РЕЗУЛЬТАТ - {}", getLastStatRequest, e.getMessage());
        }
        return null;
    }

    /**
     * Возвращает последнюю добавленную статистику печатника
     * @return статистику в формате:<br>
     * Фамилия Имя Отчество<br>
     * dd.MM.yyyy<br>
     * Напечатано: ЧИСЛО<br>
     * Брак: ЧИСЛО
     */
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
            log.info("СТАТИСТИКИ ПЕЧАТНИКА {} НЕТ. ЗАПРОС {} ВЕРНУЛ ПУСТОЙ РЕЗУЛЬТАТ - {}",currentEmployee.getFio(), getLastStatRequest, e.getMessage());
        }
        return null;
    }

    /**
     * Удяляет буфер статистики печатника
     * @param currentEmployee печатник
     */
    public void deletePrinterBuffer(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sqlDropRequest = String.format("DROP TABLE IF EXISTS %s;", bufferTableName);
        makeSqlRequestByStatement(sqlDropRequest);
    }


    /**
     * Возвращает случайную фразу для печатника из таблиц nice_words и motivation_words, в зависимости от количества напечатанной продукции.
     * @param printsNum количетво напечатанных заказов
     * @return фраза типа String
     */
    public String getNicePhraseToPrinter(int printsNum) {
        if (printsNum > 100) {
            return getRandomPhrase("nice_words");
        } else {
            return getRandomPhrase("motivation_words");
        }
    }

    /**
     * Возвращает случайную фразу из таблицы
     * @param table имя таблицы
     * @return фраза типа String
     */
    public String getRandomPhrase(String table) {
        String sqlGetRequest = String.format("SELECT (phrase) FROM %s ORDER BY RANDOM();", table);
        List<String> result = makeSelectRequest(sqlGetRequest);
        return result.getFirst();
    }

    /**
     * Создает таблицу статистики для сборки
     */
    public void createNewPackerStatisticTableIfNotExists() {
        String tableName = "statistics_by_packers";
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (date DATE PRIMARY KEY NOT NULL, wb_mhc INT DEFAULT 0, wb_signum INT DEFAULT 0, wb_silicosha INT DEFAULT 0, ozon INT DEFAULT 0, yandex INT DEFAULT 0, wb_printKid INT DEFAULT 0, fbo INT DEFAULT 0, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP);", tableName);
        makeSqlRequestByStatement(sql);
    }

    /**
     * Создает временную таблицу для сборки по id
     * @param currentEmployee сборщик
     */
    public void createNewPackerStatisticBuffer(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY NOT NULL, date DATE, wb_mhc INT DEFAULT 0, wb_signum INT DEFAULT 0, wb_silicosha INT DEFAULT 0, ozon INT DEFAULT 0, yandex INT DEFAULT 0, wb_printKid INT DEFAULT 0, fbo INT DEFAULT 0);", bufferTableName);
        makeSqlRequestByStatement(sql);
    }

    /**
     *  Добавляет значение в буфер сборщика
     * @param currentEmployee сборщик
     * @param value значение
     * @param columnName имя колонки
     */
    public void addValueInBufferFromPacker(Employee currentEmployee, Object value, String columnName) {
        String tableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String sql = String.format("INSERT INTO %s (id, %s) VALUES (1, ?) ON CONFLICT (id) DO UPDATE SET %s = EXCLUDED.%s;", tableName, columnName, columnName, columnName);
        jdbcTemplate.update(sql, value);
    }

    /**
     * Удаляет буфер сборщика
     * @param currentEmployee сборщик
     */
    public void deletePackerBuffer(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String sqlDropRequest = String.format("DROP TABLE IF EXISTS %s;", bufferTableName);
        makeSqlRequestByStatement(sqlDropRequest);
    }

    /**
     * Перености статистику из буфера сборщика в таблицу сборки
     * @param currentEmployee сборщик
     * @return добавленная статистика класса PackerStatistic
     */
    public PackerStatistic moveDataFromPackerBufferToMainTable(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String mainTableName = "statistics_by_packers";
        String sql = String.format("INSERT INTO %s (date, wb_mhc, wb_signum, wb_silicosha, ozon, yandex, wb_printKid, fbo, created_at) SELECT date,wb_mhc,wb_signum,wb_silicosha,ozon,yandex,wb_printKid,fbo, CURRENT_TIMESTAMP FROM %s ON CONFLICT (date) DO UPDATE SET date = excluded.date,wb_mhc= excluded.wb_mhc,wb_signum= excluded.wb_signum,wb_silicosha = excluded.wb_silicosha,ozon= excluded.ozon,yandex= excluded.yandex,wb_printkid= excluded.wb_printkid,fbo= excluded.fbo,created_at=CURRENT_TIMESTAMP RETURNING*;", mainTableName, bufferTableName);
        PackerStatistic statistic = jdbcTemplate.queryForObject(sql, new PackerStatisticMapper());
        deletePackerBuffer(currentEmployee);
        return statistic;
    }


    /**
     * Вспомогательный метод для проверки существования таблицы
     * @param tableName имя таблицы
     * @return true, если таблица существует
     */
    private boolean doesTableExist(String tableName) {
        String doesTableExistRequest = String.format("SELECT 1 FROM information_schema.tables WHERE table_name = '%s'", tableName);
        return makeSelectRequest(doesTableExistRequest).equals(List.of("1"));
    }

    /**
     * Вспомогательный метод для получения первого и последнего дня месяца
     * @param table имя таблицы
     * @return лист из первой и последней дат месяца типа LocalDate
     */
    //
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

    /**
     * Выполняет выборку статистики
     * @param query запрос
     * @param rowMapper упаковщик статистики
     * @return список статистики
     */
    //
    private <T> List<T> executeQuery(String query, RowMapper<T> rowMapper) {
        return jdbcTemplate.query(query, rowMapper);
    }


    /**
     * Возращет статистику печатника по chat_id за текущий указанный месяц
     * @param chatId id печатника
     * @param table имя таблицы
     * @return список статистики или null
     */
    public List<PrinterStatistic> getPrinterStatisticByChatId(long chatId, String table) {
        String tableName = String.format("statistic_from_%s", chatId);
        if (doesTableExist(tableName)) {
            List<LocalDate> dates = getFirstAndLastDayOfMonth(table);
            String selectStatRequest = String.format("SELECT * FROM %s WHERE date >= '%s' AND date <= '%s';",
                    tableName, dates.getFirst(), dates.getLast());
            return executeQuery(selectStatRequest, new PrinterStatisticMapper());
        } else {
            return null;
        }
    }


    /**
     * Возращет статистику сборки за текущий указанный месяц
     * @param table имя таблицы
     * @return список статистики или null
     */
    public List<PackerStatistic> getPackerStatistics(String table) {
        String tableName = "statistics_by_packers";
        if (doesTableExist(tableName)) {
            List<LocalDate> dates = getFirstAndLastDayOfMonth(table);
            String selectStatRequest = String.format("SELECT * FROM %s WHERE date >= '%s' AND date <= '%s';",
                    tableName, dates.getFirst(), dates.getLast());
            return executeQuery(selectStatRequest, new PackerStatisticMapper());
        } else {
            return null;
        }
    }

    /**
     * Удадяет последнюю статистику сборки
     * @return true, если успешно
     */
    public boolean deleteLastPackerRecord() {
        String deleteRequest = "DELETE FROM statistics_by_packers WHERE date = (SELECT date FROM statistics_by_packers ORDER BY created_at DESC LIMIT 1);";
        return makeSqlRequestByStatement(deleteRequest);
    }

    /**
     * Удадяет последнюю статистику печатника
     * @param currentEmployee печатник
     * @return true, если успешно
     */
    public boolean deleteLastPrinterRecord(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String deleteRequest = String.format("DELETE FROM %s WHERE date = (SELECT date FROM %s ORDER BY created_at DESC LIMIT 1);", tableName, tableName);
        return makeSqlRequestByStatement(deleteRequest);
    }

}