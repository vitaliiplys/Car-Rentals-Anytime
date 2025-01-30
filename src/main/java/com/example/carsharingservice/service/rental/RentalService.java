package com.example.carsharingservice.service.rental;

import com.example.carsharingservice.dto.rental.RentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.example.carsharingservice.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponseDto addRental(User principal, RentalRequestDto requestDto);

    List<RentalResponseDto> findRentalsById(Long userId, boolean isActive, Pageable pageable);

    RentalResponseDto findById(Long id);

    RentalResponseDto setActualReturnDate(Long id);
}
