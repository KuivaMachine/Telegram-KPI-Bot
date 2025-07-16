package org.example.kpitelegrambot.bot.configuration;

import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Сервис для импорта настроек админа (включить/выключить ежедневное оповещение)
 */
@Configuration
public class SettingsManager {

    private final Properties props = new Properties();
    private final File configFile = new File("./admin.properties");


    /**
     * Читает и возвращает настройку разрешения уведомлений (admin_notification.enabled)
     * @return true, если разрешено
     */
    public boolean isNotificationEnabled() {
        try (InputStream input = new FileInputStream(configFile)) {
            props.load(input);
            return Boolean.parseBoolean(
                    props.getProperty("admin_notification.enabled", "false")
            );
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * Устанавливает настройку разрешения уведомлений (admin_notification.enabled)
     * @param value разрешено/запрещено
     */
    public void setNotificationEnabled(boolean value) {
        props.setProperty("admin_notification.enabled", String.valueOf(value));
        try (OutputStream output = Files.newOutputStream(configFile.toPath())) {
            props.store(output, "Updated by bot command");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
