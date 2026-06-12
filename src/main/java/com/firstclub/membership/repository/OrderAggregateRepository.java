package com.firstclub.membership.repository;

import com.firstclub.membership.entity.OrderAggregate;
import com.firstclub.membership.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OrderAggregateRepository extends JpaRepository<OrderAggregate, Long> {

    OrderAggregate findByUserIdAndOrderMonthYear(Long userId, String orderMonthAndYear);

    @Query("SELECT COALESCE(SUM(o.orderCount), 0) FROM OrderAggregate o WHERE o.user = :user AND o.orderMonthYear = :month")
    int sumOrderCountByUserAndMonth(@Param("user") User user, @Param("month") String month);

    @Modifying
    @Query("UPDATE OrderAggregate o " +
            "SET o.orderCount = o.orderCount + 1, " +
            "    o.totalAmount = o.totalAmount + :amount " +
            "WHERE o.user.id = :userId AND o.orderMonthYear = :monthYear")
    int incrementAggregate(@Param("userId") Long userId,
                           @Param("monthYear") String monthYear,
                           @Param("amount") BigDecimal amount);
}
