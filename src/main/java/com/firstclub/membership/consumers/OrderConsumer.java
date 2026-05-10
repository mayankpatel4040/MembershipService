package com.firstclub.membership.consumers;

import com.firstclub.membership.dto.OrderDetail;
import com.firstclub.membership.service.OrderService;
import com.firstclub.membership.service.TierRuleEvaluatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderConsumer {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final TierRuleEvaluatorService tierRuleEvaluatorService;

    @KafkaListener(
            topics = "${app.kafka.topics.order}",
            groupId = "order-group",
            containerFactory = "orderContainerFactory"
    )
    public void consumeOrder(String message, Acknowledgment ack) {
        log.info("Order event received");
        OrderDetail orderDetail = objectMapper.readValue(message, OrderDetail.class);

        // Critical path: process the order and commit the offset atomically.
        // If processOrder throws, ack is never called → Kafka redelivers the message.
        // DefaultErrorHandler will retry retryMaxAttempts times before routing to DLT.
        orderService.processOrder(orderDetail);
        ack.acknowledge();

        // Tier evaluation is best-effort: a failure here must not trigger redelivery
        // because the order has already been committed (ack called above).
        try {
            tierRuleEvaluatorService.evaluateRules(orderDetail.getPhoneNumber());
        } catch (Exception e) {
            log.error("Tier evaluation failed for phone={} after order={} — order still committed",
                    orderDetail.getPhoneNumber(), orderDetail.getOrderId(), e);
        }
    }
}
