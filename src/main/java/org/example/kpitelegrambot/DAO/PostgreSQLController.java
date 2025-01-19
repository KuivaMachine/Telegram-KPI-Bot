package org.example.kpitelegrambot.DAO;

import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.entity.Employee;
import org.example.kpitelegrambot.service.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Log4j2
@Component
public class PostgreSQLController {


private final JdbcTemplate jdbcTemplate;


    public PostgreSQLController(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void makeSqlRequestByStatement(String sql) {
      jdbcTemplate.execute(sql);
        /*  try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            log.info(String.format("Couldn't makeSqlRequestByStatement BECAUSE: %s", e.getMessage()));
        }*/
    }

    public void makeSqlRequestByPreparedStatement(String sql, Object data) {
        jdbcTemplate.update(sql, data);
      /*  try {
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(index, data);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            log.info(String.format("Couldn't makeSqlRequestByPreparedStatement BECAUSE: %s", e.getMessage()));
        }*/
    }

    public List<String> makeSelectRequest(String sql) {
       // List<Object[]> rows = new ArrayList<>();
        return jdbcTemplate.queryForList(sql, String.class);
        /*try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Object[] row = new Object[resultSet.getMetaData().getColumnCount()];
                for (int i = 0; i < row.length; i++) {
                    row[i] = resultSet.getObject(i + 1);
                }
                rows.add(row);
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            log.warn(String.format("Не удалось выполнить SELECT запрос %s по причине %s", sql, e.getMessage()));
        }*/

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
                "ORDER BY date DESC " +
                "LIMIT 1;", tableName);
        StringBuilder sb = new StringBuilder();
        List<String> rows = makeSelectRequest(getLastStatRequest);
            sb.append(DateService.parseSqlDateToString(rows.getFirst()))
                    .append("\n")
                    .append("WB основной: ").append(rows.get(1))
                    .append("\n")
                    .append("ЕБ: ").append(rows.get(2))
                    .append("\n")
                    .append("СЛ: ").append(rows.get(3))
                    .append("\n")
                    .append("Озон: ").append(rows.get(4))
                    .append("\n")
                    .append("Яндекс: ").append(rows.get(5))
                    .append("\n")
                    .append("WB Print Kid: ").append(rows.get(6))
                    .append("\n")
                    .append("ФБО: ").append(rows.get(7));

        return sb.toString();
    }

    public String getLastAddedPrinterRecord(Employee currentEmployee) {
        String getFioSqlRequest = String.format("SELECT fio FROM employees WHERE chat_id = %d;", currentEmployee.getChatId());
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String getLastStatRequest = String.format("SELECT date, prints_num, defects_num " +
                "FROM %s " +
                "ORDER BY id DESC " +
                "LIMIT 1;", tableName);
        StringBuilder sb = new StringBuilder();
        List<String> rows = makeSelectRequest(getFioSqlRequest);

        sb.append(rows.getFirst()).append("\n");

        List<String> rows2 = makeSelectRequest(getLastStatRequest);

            sb.append(DateService.parseSqlDateToString(rows2.get(0)))
                    .append("\n")
                    .append("Напечатано: ").append(rows2.get(1))
                    .append("\n")
                    .append("Брак: ").append(rows2.get(2));

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
        String motivationPhrase = "Неплохо!";
        Random random = new Random();
        int randInt = random.nextInt(10) + 1;
        String sqlGetRequest = String.format("SELECT (phrase) FROM motivation_words WHERE id = %d;", randInt);
        List<String> result = makeSelectRequest(sqlGetRequest);

            motivationPhrase = result.get(0).toString();

        return motivationPhrase;
    }

    public String getNicePhrase() {
        String nicePhrase = "Невероятная работа!";
        Random random = new Random();
        int randInt = random.nextInt(59) + 1;
        String sqlGetRequest = String.format("SELECT (phrase) FROM nice_words WHERE id = %d;", randInt);
        List<String> result = makeSelectRequest(sqlGetRequest);

            nicePhrase = result.get(0).toString();

        return nicePhrase;
    }

    public void createNewPackerStatisticTableIfNotExists() {
        String tableName = "statistics_by_packers";
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (date DATE PRIMARY KEY NOT NULL, wb_mhc INT, wb_signum INT, wb_silicosha INT, ozon INT, yandex INT, wb_printKid INT, fbo INT);", tableName);
        makeSqlRequestByStatement(sql);
    }

    public void createNewPackerStatisticBuffer(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY NOT NULL, date DATE, wb_mhc INT, wb_signum INT, wb_silicosha INT, ozon INT, yandex INT, wb_printKid INT, fbo INT);", bufferTableName);
        makeSqlRequestByStatement(sql);
    }

    public void addValueInBufferFromPacker(Employee currentEmployee, Object value, String columnName) {
        String tableName = String.format("statistic_buffer_from_packer_%s", currentEmployee.getChatId());
        String sql = String.format("INSERT INTO %s (id, %s) VALUES (1, ?)) ON CONFLICT (id) DO UPDATE SET %s = EXCLUDED.%s;", tableName, columnName, columnName, columnName);
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
                "INSERT INTO %s (date, wb_mhc, wb_signum, wb_silicosha, ozon, yandex, wb_printKid, fbo) " +
                "SELECT date, wb_mhc, wb_signum, wb_silicosha, ozon, yandex, wb_printKid, fbo " +
                "FROM %s;" +
                "DROP TABLE %s;" +
                "COMMIT;", mainTableName, bufferTableName, bufferTableName);
        makeSqlRequestByStatement(sql);
    }


}
