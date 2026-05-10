package com.firstclub.membership.consumers;

import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class PaymentConsumer {

    @KafkaListener(
            topics = "${app.kafka.topics.payment}",
            groupId = "payment-group",
            containerFactory = "paymentContainerFactory"
    )
    public void consumePayment(String message, Acknowledgment ack) {
        // TODO: implement payment tracking against MembershipPaymentTransactions
        log.info("Payment event received (unprocessed): {}", message);
        ack.acknowledge();
    }
}
