package com.firstclub.membership.controller;

import com.firstclub.membership.dto.OrderDetail;
import com.firstclub.membership.service.KafkaProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/kafka")
@RequiredArgsConstructor
@Tag(name = "Kafka Events",
        description = "Test endpoints for publishing order events. In production these are triggered by upstream services.")
public class KafkaEventController {

    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @PostMapping("/sendOrderEvent")
    @Operation(
            summary = "Publish an order event",
            description = "Simulates an order placed by an upstream service. Drives monthly aggregation and tier re-evaluation via OrderConsumer.")
    public String sendOrderEvent(@Valid @RequestBody OrderDetail orderDetail) {
        String message = objectMapper.writeValueAsString(orderDetail);
        kafkaProducerService.sendOrderEvent(message);
        return "Order event sent: " + message;
    }
}
