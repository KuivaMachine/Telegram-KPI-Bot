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
    public final KafkaTemplate<String, PrinterStatistic> kafkaTemplate;

    public void send(String topic, PrinterStatistic statistic) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValueAsString(statistic);
            kafkaTemplate.send(topic, statistic);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
}
