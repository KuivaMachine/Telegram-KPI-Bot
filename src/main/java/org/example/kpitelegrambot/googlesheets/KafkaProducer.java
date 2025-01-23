package org.example.kpitelegrambot.googlesheets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.kpitelegrambot.DAO.entity.PrinterStatistic;
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
    public final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, PrinterStatistic statistic) {
        SendResult<String, String> result = null;
        try {

ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(statistic);
            result = kafkaTemplate.send(topic, json).get();
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
}
