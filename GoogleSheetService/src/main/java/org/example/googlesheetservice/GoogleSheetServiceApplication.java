package org.example.googlesheetservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.googlesheetservice.SheetsServices.GoogleSheetsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;




@SpringBootApplication
@ComponentScan(basePackages = {"org.example.postgresql", "org.example.googlesheetservice"})
@EnableScheduling
public class GoogleSheetServiceApplication {


    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(GoogleSheetServiceApplication.class, args);
    }

}
