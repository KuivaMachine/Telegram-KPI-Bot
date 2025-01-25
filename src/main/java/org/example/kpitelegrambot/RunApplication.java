package org.example.kpitelegrambot;

import org.example.kpitelegrambot.bot.configuration.TelegramBotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({TelegramBotConfig.class})
public class RunApplication {



    public static void main(String[] args) {

        SpringApplication.run(RunApplication.class, args);
    }

}
