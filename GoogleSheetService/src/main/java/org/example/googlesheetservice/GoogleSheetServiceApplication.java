package org.example.googlesheetservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.example.postgresql","org.example.googlesheetservice"})
public class GoogleSheetServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoogleSheetServiceApplication.class, args);
    }

}
