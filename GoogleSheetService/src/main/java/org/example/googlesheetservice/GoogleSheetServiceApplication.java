package org.example.googlesheetservice;

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
        SpringApplication.run(GoogleSheetServiceApplication.class, args);
    }

}
