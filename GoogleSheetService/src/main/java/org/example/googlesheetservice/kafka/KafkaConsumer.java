package org.example.googlesheetservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.googlesheetservice.Data.PrinterStatistic;
import org.example.googlesheetservice.SheetsServices.GoogleSheetsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final GoogleSheetsService googleSheetsService;

    public KafkaConsumer(GoogleSheetsService googleSheetsService) {
        this.googleSheetsService = googleSheetsService;
    }

    @KafkaListener(topics = "printer_stat_topic", groupId = "kpi_mhc")
    public void receive(ConsumerRecord<String, String> record) {
        PrinterStatistic statistic;
        try {
            statistic = objectMapper.readValue(record.value(), PrinterStatistic.class);
            googleSheetsService.addPrinterStatistic(statistic);
            log.info(statistic.toString());
        } catch (JsonProcessingException e) {
            log.error(String.format("Printer statistics had not been received, cause: %s",e.getMessage()));
        }

    }
}
