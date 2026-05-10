package com.firstclub.membership.repository;

import com.firstclub.membership.entity.ProcessedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, Long> {
    boolean existsByOrderId(String orderId);
}
