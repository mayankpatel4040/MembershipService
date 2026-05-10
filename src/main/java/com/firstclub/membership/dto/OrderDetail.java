package com.firstclub.membership.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderDetail {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Order value is required")
    @DecimalMin(value = "0.01", message = "Order value must be greater than zero")
    private BigDecimal orderValue;
}
