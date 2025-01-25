package org.example.googlesheetservice;

import org.example.googlesheetservice.SheetsServices.GoogleSheetsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.security.GeneralSecurityException;

@SpringBootApplication

public class GoogleSheetServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoogleSheetServiceApplication.class, args);
    }

}
