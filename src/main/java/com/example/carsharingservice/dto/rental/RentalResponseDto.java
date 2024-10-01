package com.example.carsharingservice.dto.rental;

import java.time.LocalDate;

public record RentalResponseDto(
        Long id,
        LocalDate rentalDate,
        LocalDate actualReturnDate,
        Long carId,
        Long userId,
        boolean active
) {
}
