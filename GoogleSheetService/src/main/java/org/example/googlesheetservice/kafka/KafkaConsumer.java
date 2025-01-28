package org.example.googlesheetservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.googlesheetservice.Data.KafkaCommands;
import org.example.googlesheetservice.Data.PrinterStatistic;
import org.example.googlesheetservice.SheetsServices.GoogleSheetsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final GoogleSheetsService googleSheetsService;
    ConcurrentHashMap<KafkaCommands, Boolean> commandStates = new ConcurrentHashMap<>();

    public KafkaConsumer(GoogleSheetsService googleSheetsService) {
        this.googleSheetsService = googleSheetsService;
    }

    @KafkaListener(topics = "printer_stat_topic", groupId = "kpi_mhc")
    public void receive(ConsumerRecord<String, String> record, Acknowledgment ack) {
        ack.acknowledge();
        PrinterStatistic statistic;
        try {
            statistic = objectMapper.readValue(record.value(), PrinterStatistic.class);
            googleSheetsService.addPrinterStatistic(statistic);
            log.info(statistic.toString());

        } catch (Exception e) {
            log.error(String.format("Printer statistics had not been received, cause: %s", e.getMessage()));
        }
    }

    @KafkaListener(topics = "commands", groupId = "kpi_mhc")
    @Transactional
    public void receiveCommand(ConsumerRecord<String, String> record, Acknowledgment ack) {
        ack.acknowledge();

        KafkaCommands command;
        try {
            command = KafkaCommands.valueOf(objectMapper.readValue(record.value(), String.class));
            log.info(command.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (commandStates.putIfAbsent(command, true) != null) {
            System.out.println("Command " + command + " is already being processed.");
            return;
        }
        CompletableFuture.runAsync(() -> googleSheetsService.fullUpdateTable())
                .exceptionally(ex -> {
                    // Обработка ошибок
                    log.warn("Error creating table: {}", ex.getMessage());
                    return null;
                });
        commandStates.remove(command);

    }
}

