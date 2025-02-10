package org.example.kpitelegrambot;

import org.example.kpitelegrambot.bot.configuration.TelegramBotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties({TelegramBotConfig.class})
@ComponentScan(basePackages = {
        "org.example.postgresql","org.example.kpitelegrambot"})

public class DispatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispatcherApplication.class, args);
    }

}
