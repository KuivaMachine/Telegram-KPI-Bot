package org.example.googlesheetservice;

import org.example.googlesheetservice.SheetsServices.GoogleSheetsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.security.GeneralSecurityException;

@SpringBootApplication
public class GoogleSheetServiceApplication {

	public static void main(String[] args) {
		/*GoogleSheetsService googleSheetsService = new GoogleSheetsService(15,5);
        try {
            googleSheetsService.createNewSheet();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }*/
        SpringApplication.run(GoogleSheetServiceApplication.class, args);
	}

}
