package com.firstclub.membership.repository;

import com.firstclub.membership.entity.OrderAggregate;
import com.firstclub.membership.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderAggregateRepository extends JpaRepository<OrderAggregate, Long> {

    List<OrderAggregate> findByUserId(User userId);

    OrderAggregate findByOrderMonthYear(String orderMonthAndYear);

    OrderAggregate findByUserIdAndOrderMonthYear(Long userId, String orderMonthAndYear);

    @Modifying
    @Query("UPDATE OrderAggregate o " +
            "SET o.orderCount = o.orderCount + 1, " +
            "    o.totalAmount = o.totalAmount + :amount " +
            "WHERE o.userId.id = :userId AND o.orderMonthYear = :monthYear")
    int incrementAggregate(@Param("userId") Long userId,
                           @Param("monthYear") String monthYear,
                           @Param("amount") BigDecimal amount);
}

