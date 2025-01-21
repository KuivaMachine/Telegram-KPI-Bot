package org.example.googlesheetservice.kafka;

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
    public void receive (ConsumerRecord<String, PrinterStatistic> record) {
        log.info("Афигеть, я принял сообщение!!! - "+record.value());
    }
}
