package com.example.carsharingservice.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CarRequestDto(
        @NotBlank
        String model,
        @NotBlank
        String brand,
        @NotBlank
        String type,
        @Positive
        int inventory,
        @Positive
        BigDecimal dailyFee
) {
}
