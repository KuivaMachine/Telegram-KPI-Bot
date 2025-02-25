package org.example.googlesheetservice;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;




@Log4j2
@SpringBootApplication
@EnableScheduling
public class GoogleSheetServiceApplication {


    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(GoogleSheetServiceApplication.class, args);
        log.info("НОВАЯ ВЕРСИЯ_1");
    }

}
