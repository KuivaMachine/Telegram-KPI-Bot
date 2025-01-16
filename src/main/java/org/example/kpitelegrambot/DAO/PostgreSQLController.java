package org.example.kpitelegrambot.DAO;

import org.example.kpitelegrambot.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;


@Component
public class PostgreSQLController {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLController.class);
    String username = "kuiva";
    String url = "jdbc:postgresql://localhost:5432/test_DB";
    String password = "p@ssw0rd";

    public void deleteTable(String employees) {
        String sql = "DROP TABLE ?";
        makeSqlRequest(sql);
    }

    public void makeSqlRequest(String sql) {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createNewStatisticTableIfNotExists(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY, date DATE, prints_num INT, defects_num INT);", tableName);
        makeSqlRequest(sql);
    }

    public void createNewStatisticBuffer(Employee currentEmployee) {
        String tableName = String.format("statistic_buffer_from_%s", currentEmployee.getChatId());
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (id SERIAL PRIMARY KEY, date DATE, prints_num INT, defects_num INT);", tableName);
        makeSqlRequest(sql);
    }

    public void addDateInBuffer(Employee currentEmployee, Date date) {
        String tableName = String.format("statistic_buffer_from_%s", currentEmployee.getChatId());
        String sql = "INSERT INTO " + tableName + " (date) VALUES (?);";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDate(1, date);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            log.info(String.format("Couldn't insert DATE %s into table %s", date, tableName));
        }
    }

    public void addPrintsNumberInBuffer(Employee currentEmployee, int printsNum) {
        String tableName = String.format("statistic_buffer_from_%s", currentEmployee.getChatId());
        String sql = "UPDATE " + tableName + " SET prints_num = (?) WHERE id=1;";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, printsNum);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            log.info(String.format("Couldn't insert print number %d into table %s \n Reason: %s", printsNum, tableName, e.getMessage()));
        }
    }

    public void addDefectsNumberInBuffer(Employee currentEmployee, int numberOfDefects) {
        String tableName = String.format("statistic_buffer_from_%s", currentEmployee.getChatId());
        String sql = "UPDATE " + tableName + " SET defects_num = (?) WHERE id=1;";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, numberOfDefects);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            log.info(String.format("Couldn't insert defects number %d into table %s", numberOfDefects, tableName));
        }
    }

    public void moveDataFromBufferToMainTable(Employee currentEmployee) {
        String bufferTableName = String.format("statistic_buffer_from_%s", currentEmployee.getChatId());
        String mainTableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String sql = String.format("BEGIN TRANSACTION; " +
                "INSERT INTO %s (date, prints_num, defects_num) " +
                "SELECT date, prints_num, defects_num " +
                "FROM %s;" +
                "DROP TABLE %s;" +
                "COMMIT;", mainTableName, bufferTableName, bufferTableName);
        makeSqlRequest(sql);

    }

    public String getLastAddedRecord(Employee currentEmployee) {
        String tableName = String.format("statistic_from_%s", currentEmployee.getChatId());
        String getFioSqlRequest = String.format("SELECT fio FROM employees WHERE chat_id = %d;", currentEmployee.getChatId());

        String getLastStatRequest = String.format("SELECT date, prints_num, defects_num " +
                        "FROM %s " +
                        "ORDER BY id DESC " +
                        "LIMIT 1;",tableName);
        StringBuilder sb = new StringBuilder();
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(getFioSqlRequest);
            if (rs.next()) {
                sb.append(rs.getString("fio")).append("\n");
            }else{
                log.info(String.format("Запрос %s не вернул результатов.",getFioSqlRequest));
            }
            rs.close();
            ResultSet rs2 = statement.executeQuery(getLastStatRequest);
            if (rs2.next()) {
                sb.append(rs2.getDate("date"))
                        .append("\n")
                        .append("Напечатано: ").append(rs2.getString("prints_num"))
                        .append("\n")
                        .append("Брак: ").append(rs2.getString("defects_num"));
            }else{
                log.info(String.format("Запрос %s не вернул результатов.",getLastStatRequest));
            }

            rs2.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
           log.warn(e.getMessage());
        }
        return sb.toString();
    }
}
