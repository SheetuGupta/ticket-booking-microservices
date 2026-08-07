package com.ticketbooking.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name("payment-events-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingNotificationsTopic() {
        return TopicBuilder.name("booking-notifications-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
