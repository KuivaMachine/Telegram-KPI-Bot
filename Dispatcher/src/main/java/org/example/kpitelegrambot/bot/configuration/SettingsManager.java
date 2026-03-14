package org.example.kpitelegrambot.bot.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Сервис для импорта настроек админа (включить/выключить ежедневное оповещение)
 */
@Configuration
@RequiredArgsConstructor
public class SettingsManager {

    private final JdbcTemplate jdbcTemplate;


    /**
     * Читает и возвращает настройку разрешения уведомлений (admin_notification_enabled)
     * @return true, если разрешено
     */
    public boolean isNotificationEnabled() {
        try {
            List<String> result = jdbcTemplate.queryForList("SELECT value FROM settings WHERE sys_name = 'admin_notification_enabled'", String.class);
            if (!result.isEmpty()) {
                return Boolean.parseBoolean(result.getFirst());
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Устанавливает настройку разрешения уведомлений (admin_notification_enabled)
     * @param value разрешено/запрещено
     */
    public void setNotificationEnabled(boolean value) {
        jdbcTemplate.update("UPDATE settings SET value = ? WHERE sys_name = 'admin_notification_enabled'", String.valueOf(value));
    }

}
