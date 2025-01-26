package org.example.kpitelegrambot;

import org.example.kpitelegrambot.bot.configuration.TelegramBotConfig;
import org.example.postgresql.DAO.PostgresConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties({TelegramBotConfig.class})
@ComponentScan(basePackages = {
        "org.example.postgresql","org.example.kpitelegrambot"})

public class RunDispatcher {

    public static void main(String[] args) {
        SpringApplication.run(RunDispatcher.class, args);
    }

}
