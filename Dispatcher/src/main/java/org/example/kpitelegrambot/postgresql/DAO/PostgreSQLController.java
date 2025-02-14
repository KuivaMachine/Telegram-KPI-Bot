package org.example.postgresql.DAO;

import lombok.extern.log4j.Log4j2;
import org.example.postgresql.data.Months;
import org.example.postgresql.entity.*;
import org.example.postgresql.service.DateService;
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
    }

    private void makeSqlRequestByStatement(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
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

        PackerStatistic statistic = jdbcTemplate.queryForObject(getLastStatRequest, new PackerStatisticMapper());

        StringBuilder sb = new StringBuilder();

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
        }
        return sb.toString();
    }

    public String getLastAddedPrinterRecordToString(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String getLastStatRequest = String.format("SELECT date, prints_num, defects_num FROM %s ORDER BY created_at DESC LIMIT 1;", tableName);
        StringBuilder sb = new StringBuilder();
        sb.append(currentEmployee.getFio()).append("\n");
        PrinterStatistic statistic = jdbcTemplate.queryForObject(getLastStatRequest, new PrinterStatisticMapper());
        if (statistic != null) {
            sb.append(DateService.parseSqlDateToString(statistic.getDate()))
                    .append("\n")
                    .append("Напечатано: ").append(statistic.getPrints_num())
                    .append("\n")
                    .append("Брак: ").append(statistic.getDefects_num());
        }

        return sb.toString();
    }

    public void deletePrinterBuffer(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sqlDropRequest = String.format("DROP TABLE %s;", bufferTableName);
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
        String sqlDropRequest = String.format("DROP TABLE %s;", bufferTableName);
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

}