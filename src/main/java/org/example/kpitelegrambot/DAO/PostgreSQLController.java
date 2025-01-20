package org.example.kpitelegrambot.DAO;

import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.DAO.entity.Employee;
import org.example.kpitelegrambot.DAO.entity.PackerStatistic;
import org.example.kpitelegrambot.DAO.entity.PrinterStatistic;
import org.example.kpitelegrambot.service.DateService;
import org.example.kpitelegrambot.service.EmployeeService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Log4j2
@Component
public class PostgreSQLController {


    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public PostgreSQLController(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void makeSqlRequestByStatement(String sql) {
        jdbcTemplate.execute(sql);
    }

    public void makeSqlRequestByPreparedStatement(String sql, Object data) {
        jdbcTemplate.update(sql, data);
    }

    public List<String> makeSelectRequest(String sql) {
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public void createNewStatisticTableIfNotExists(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY, date DATE, prints_num INT, defects_num INT);", tableName);
        makeSqlRequestByStatement(sql);
    }

    public void createNewStatisticBuffer(Employee currentEmployee) {
        String tableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY, date DATE, prints_num INT, defects_num INT);", tableName);
        makeSqlRequestByStatement(sql);
    }


    public void addValueInBufferFromPrinter(Employee currentEmployee, Object value, String columnName) {
        String tableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String sql = String.format("INSERT INTO %s (id, %s) VALUES (1, ?) ON CONFLICT (id) DO UPDATE SET %s = EXCLUDED.%s;", tableName, columnName, columnName, columnName);
        makeSqlRequestByPreparedStatement(sql, value);
    }

    public void moveDataFromPrinterBufferToMainTable(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_printer_%s", currentEmployee.getChatId());
        String mainTableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String sql = String.format("BEGIN TRANSACTION; " +
                "INSERT INTO %s (date, prints_num, defects_num) " +
                "SELECT date, prints_num, defects_num " +
                "FROM %s;" +
                "DROP TABLE %s;" +
                "COMMIT;", mainTableName, bufferTableName, bufferTableName);
        makeSqlRequestByStatement(sql);

    }

    public String getLastAddedPackerRecord() {
        String tableName = "statistics_by_packers";
        String getLastStatRequest = String.format("SELECT * " +
                "FROM %s " +
                "ORDER BY date_column DESC " +
                "LIMIT 1;", tableName);

        PackerStatistic statistic = jdbcTemplate.queryForObject(getLastStatRequest, new PackerStatisticMapper());

        StringBuilder sb = new StringBuilder();

        if (statistic != null) {
            sb.append(DateService.parseSqlDateToString(statistic.getDate_column()))
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

    public String getLastAddedPrinterRecord(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String getLastStatRequest = String.format("SELECT date, prints_num, defects_num " +
                "FROM %s " +
                "ORDER BY id DESC " +
                "LIMIT 1;", tableName);
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
        int randInt = random.nextInt(59) + 1;
        String sqlGetRequest = String.format("SELECT (phrase) FROM nice_words WHERE id = %d;", randInt);
        List<String> result = makeSelectRequest(sqlGetRequest);

        nicePhrase = result.getFirst();

        return nicePhrase;
    }

    public void createNewPackerStatisticTableIfNotExists() {
        String tableName = "statistics_by_packers";
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (date_column DATE PRIMARY KEY NOT NULL, wb_mhc INT, wb_signum INT, wb_silicosha INT, ozon INT, yandex INT, wb_printKid INT, fbo INT);", tableName);
        makeSqlRequestByStatement(sql);
    }

    public void createNewPackerStatisticBuffer(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY NOT NULL, date_column DATE, wb_mhc INT, wb_signum INT, wb_silicosha INT, ozon INT, yandex INT, wb_printKid INT, fbo INT);", bufferTableName);
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

    public void moveDataFromPackerBufferToMainTable(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String mainTableName = "statistics_by_packers";
        String sql = String.format("BEGIN TRANSACTION; " +
                "INSERT INTO %s (date_column, wb_mhc, wb_signum, wb_silicosha, ozon, yandex, wb_printKid, fbo) " +
                "SELECT date_column, wb_mhc, wb_signum, wb_silicosha, ozon, yandex, wb_printKid, fbo " +
                "FROM %s;" +
                "DROP TABLE %s;" +
                "COMMIT;", mainTableName, bufferTableName, bufferTableName);
        makeSqlRequestByStatement(sql);
    }


    public boolean isAddedPackerStatisticToday() {
        Date date = Date.valueOf(LocalDate.now());
        String sql = "SELECT date_column FROM statistics_by_packers WHERE date_column = ?;";
        try (Connection connection= dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDate(1, date);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                resultSet.close();
                preparedStatement.close();
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
