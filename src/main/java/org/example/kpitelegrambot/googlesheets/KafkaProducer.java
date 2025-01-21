package org.example.kpitelegrambot.googlesheets;

import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.DAO.entity.PrinterStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    public final KafkaTemplate<String, PrinterStatistic> kafkaTemplate;

    public void send(String topic, PrinterStatistic statistic) {
        SendResult<String, PrinterStatistic> result = null;
        try {
            result = kafkaTemplate.send(topic, statistic).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


    }
}
