package org.example.kpitelegrambot.DAO;

import lombok.RequiredArgsConstructor;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@RequiredArgsConstructor

public class PostgresListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(PostgresListener.class);
    private final DataSource dataSource;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try (Connection conn = dataSource.getConnection()) {
            while (true) {
                PGNotification[] notifications = conn.unwrap(PGConnection.class).getNotifications();
                if (notifications != null) {
                    for (PGNotification notification : notifications) {
                       log.info("Received notification: " + notification.getName());
                        log.info("Payload: " + notification.getParameter());
                    }
                }

                Statement stmt = conn.createStatement();
                stmt.execute("LISTEN new_data_channel");
                stmt.close();

                while (true) {
                    Thread.sleep(1000); // Ждем 5 секунд перед следующей проверкой

                    // Получаем новые уведомления
                    notifications = conn.unwrap(org.postgresql.PGConnection.class).getNotifications();
                    if (notifications != null) {
                        for (PGNotification notification : notifications) {
                            log.info("Received notification: " + notification.getName());
                            log.info("Payload: " + notification.getParameter());

                        }
                    }
                }
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
