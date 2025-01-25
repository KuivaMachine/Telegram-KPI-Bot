package org.example.kpitelegrambot.googlesheets;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfiguration {

    @Bean
    public NewTopic createNewTopic(){
        return new NewTopic("printer_stat_topic", 2, (short) 1);
    }
}
