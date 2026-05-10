package com.firstclub.membership.service;

import com.firstclub.membership.config.KafkaTopicConfig;
import com.firstclub.membership.dto.OrderDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicConfig kafkaTopicConfig;

    public void sendPaymentEvent(String message) {
        kafkaTemplate.send(
                kafkaTopicConfig.getPaymentTopic(),
                message
        );
    }

    public void sendOrderEvent(String message) {
        kafkaTemplate.send(
                kafkaTopicConfig.getOrderTopic(),
                message
        );
    }
}
