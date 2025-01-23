package org.example.googlesheetservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.googlesheetservice.Data.PrinterStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "printer_stat_topic", groupId = "kpi_mhc")
    public void receive (ConsumerRecord<String, String> record) {
ObjectMapper objectMapper = new ObjectMapper();
        PrinterStatistic statistic = null;
        try {
            statistic = objectMapper.readValue(record.value(), PrinterStatistic.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info(statistic.toString());
    }
}
