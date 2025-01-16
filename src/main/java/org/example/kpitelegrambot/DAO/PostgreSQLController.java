package org.example.kpitelegrambot.DAO;

import org.example.kpitelegrambot.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class PostgreSQLController {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLController.class);
    String username="kuiva";
    String url="jdbc:postgresql://localhost:5432/test_DB";
    String password="p@ssw0rd";

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
        String tableName = String.format("statistic_from_%s",currentEmployee.getChatId());
        String sql =String.format("CREATE TABLE IF NOT EXISTS %s (date DATE NOT NULL PRIMARY KEY, prints_num INT, defects_num INT);", tableName);
        makeSqlRequest(sql);
//РАЗОБРАТЬСЯ, ГДЕ ТАБЛИЦА ПЕЧАТНИКА, ХОТЯ БУФЕР СОЗДАЛСЯ. И ПИСАТЬ ДАЛЬШЕ
    }

    public void createNewStatisticBuffer(Employee currentEmployee) {
        String tableName = String.format("statistic_buffer_from_%s",currentEmployee.getChatId());
        String sql =String.format("CREATE TABLE IF NOT EXISTS %s (date DATE NOT NULL PRIMARY KEY, prints_num INT, defects_num INT);", tableName);
        makeSqlRequest(sql);
    }

    public void addDateInBuffer(Employee currentEmployee, Date date) {
        String tableName = String.format("statistic_buffer_from_%s",currentEmployee.getChatId());
        String sql = "INSERT INTO "+tableName +" (date) VALUES (?)";
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDate(1, date);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            log.info(date.toString());
        }
    }



}
