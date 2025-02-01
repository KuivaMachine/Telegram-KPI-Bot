package org.example.kpitelegrambot.googlesheets;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class KafkaConfiguration {

    @Bean
    public NewTopic createNewTopicPrinter(){
        return new NewTopic("printer_stat_topic", 2, (short) 1);
    }
    @Bean
    public NewTopic createNewTopicPacker(){
        return new NewTopic("packer_stat_topic", 2, (short) 1);
    }
    @Bean
    public NewTopic createNewTopicCommand(){
        return new NewTopic("commands", 2, (short) 1);
    }
}
