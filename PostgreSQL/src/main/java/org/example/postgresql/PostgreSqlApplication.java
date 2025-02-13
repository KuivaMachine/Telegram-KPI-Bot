package org.example.postgresql;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
@Log4j2
@SpringBootApplication
public class PostgreSqlApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(PostgreSqlApplication.class, args);
        log.info("Application started");
        log.warn("Application started");
        log.error("Application started");
        log.debug("Application started");
        log.trace("Application started");
    }

}
