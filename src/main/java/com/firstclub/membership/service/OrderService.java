package com.firstclub.membership.service;

import com.firstclub.membership.dto.OrderDetail;
import com.firstclub.membership.entity.OrderAggregate;
import com.firstclub.membership.entity.ProcessedOrder;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.exception.UserNotFoundException;
import com.firstclub.membership.repository.OrderAggregateRepository;
import com.firstclub.membership.repository.ProcessedOrderRepository;
import com.firstclub.membership.repository.UserRepository;
import com.firstclub.membership.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {

    private final UserRepository userRepository;
    private final OrderAggregateRepository orderAggregateRepository;
    private final ProcessedOrderRepository processedOrderRepository;

    @Transactional
    public void processOrder(OrderDetail orderDetail) {
        // Idempotent Consumer Pattern: skip orders already processed (Kafka redelivery guard)
        if (processedOrderRepository.existsByOrderId(orderDetail.getOrderId())) {
            log.warn("Order {} already processed, skipping", orderDetail.getOrderId());
            return;
        }

        log.info("Processing order {}", orderDetail.getOrderId());

        User user = userRepository.findByPhoneNumber(orderDetail.getPhoneNumber());
        if (user == null) {
            throw new UserNotFoundException(orderDetail.getPhoneNumber());
        }

        String orderMonthYear = DateTimeUtil.getCurrentYearAndMonth();

        int updated = orderAggregateRepository.incrementAggregate(
                user.getId(), orderMonthYear, orderDetail.getOrderValue());
        if (updated == 0) {
            try {
                orderAggregateRepository.save(OrderAggregate.builder()
                        .userId(user)
                        .orderMonthYear(orderMonthYear)
                        .orderCount(1)
                        .totalAmount(orderDetail.getOrderValue())
                        .build());
            } catch (DataIntegrityViolationException raceLost) {
                log.debug("Concurrent insert for user {} / {}, retrying increment", user.getId(), orderMonthYear);
                orderAggregateRepository.incrementAggregate(user.getId(), orderMonthYear, orderDetail.getOrderValue());
            }
        }

        processedOrderRepository.save(ProcessedOrder.builder()
                .orderId(orderDetail.getOrderId())
                .build());
    }
}
