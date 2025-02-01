package org.example.kpitelegrambot.googlesheets;

import lombok.RequiredArgsConstructor;
import org.example.postgresql.entity.PackerStatistic;
import org.example.postgresql.entity.PrinterStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    public final KafkaTemplate<String, PrinterStatistic> kafkaTemplatePrinter;
    public final KafkaTemplate<String, PackerStatistic> kafkaTemplatePacker;
    public final KafkaTemplate<String, String> kafkaTemplateCommand;

    public void send(String topic, PrinterStatistic statistic) {
        SendResult<String, PrinterStatistic> result;
        try {
            result = kafkaTemplatePrinter.send(topic, statistic).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        log.info(String.format("Отправил статистику печатника %s по каналу - %s", statistic, result.getRecordMetadata()));
    }

    public void send(String topic, PackerStatistic statistic) {
        SendResult<String, PackerStatistic> result;
        try {
            result = kafkaTemplatePacker.send(topic, statistic).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        log.info(String.format("Отправил статистику сборщика %s по каналу - %s", statistic, result.getRecordMetadata()));
    }


    public void send(String topic, String command) {
        SendResult<String, String> result = null;
        try {
            result = kafkaTemplateCommand.send(topic, command).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        log.info(result.getRecordMetadata().toString());
    }
}
