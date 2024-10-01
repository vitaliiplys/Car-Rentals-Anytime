package com.example.carsharingservice.dto.rental;

import java.time.LocalDate;

public record RentalRequestDto(
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        Long carId,
        Long userId
) {
}
