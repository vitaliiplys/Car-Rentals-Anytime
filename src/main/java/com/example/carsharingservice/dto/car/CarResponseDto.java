package com.example.carsharingservice.dto.car;

import com.example.carsharingservice.model.Car;
import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        String model,
        String brand,
        Car.Type type,
        int inventory,
        BigDecimal dailyFee
) {
}
