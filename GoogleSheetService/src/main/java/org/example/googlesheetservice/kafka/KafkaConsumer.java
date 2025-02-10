package org.example.googlesheetservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.googlesheetservice.Data.KafkaCommands;
import org.example.googlesheetservice.Data.PrinterStatistic;
import org.example.googlesheetservice.StatisticHandler;
import org.example.postgresql.entity.PackerStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final StatisticHandler statisticHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    ConcurrentHashMap<KafkaCommands, Boolean> commandStates = new ConcurrentHashMap<>();


    @KafkaListener(topics = "printer_stat_topic", groupId = "kpi_mhc")
    public void receivePrinterStatistic(ConsumerRecord<String, String> record, Acknowledgment ack) {
        ack.acknowledge();
        PrinterStatistic statistic;
        try {
            statistic = objectMapper.readValue(record.value(), PrinterStatistic.class);
            log.info("ПРИНЯЛ СТАТИСТИКУ ПЕЧАТНИКА В БЛОКЕ @KafkaListener(topics = \"printer_stat_topic\", groupId = \"kpi_mhc\"): "+ statistic);
            statisticHandler.processPrinterStatistic(statistic);

        } catch (Exception e) {
            log.error(String.format("Printer statistics had not been received in @KafkaListener(topics = \"printer_stat_topic\", groupId = \"kpi_mhc\"), cause: %s", e.getMessage()));
        }
    }


    @KafkaListener(topics = "packer_stat_topic", groupId = "kpi_mhc")
    public void receivePackerStatistic(ConsumerRecord<String, String> record, Acknowledgment ack) {
        ack.acknowledge();
        PackerStatistic statistic;
        try {
            statistic = objectMapper.readValue(record.value(), PackerStatistic.class);
            log.info("ПРИНЯЛ СТАТИСТИКУ СБОРЩИКА В БЛОКЕ @KafkaListener(topics = \"packer_stat_topic\", groupId = \"kpi_mhc\"): "+ statistic);
            statisticHandler.processPackerStatistic(statistic);

        } catch (Exception e) {
            log.error(String.format("Printer statistics had not been received in @KafkaListener(topics = \"printer_stat_topic\", groupId = \"kpi_mhc\"), cause: %s", e.getMessage()));
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

        CompletableFuture.runAsync(() -> statisticHandler.processUpdateTable())
                .exceptionally(ex -> {
                    // Обработка ошибок
                    log.warn("ОШИБКА В @KafkaListener(topics = \"commands\", groupId = \"kpi_mhc\") : {}", ex.getMessage());
                    return null;
                });
        commandStates.remove(command);

    }
}

