package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.rental.RentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.service.rental.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cars controller", description = "Endpoints for managing cars")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
public class RentalsController {
    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new rental",
            description = "Endpoint for creating and saving a new rental")
    public RentalResponseDto addRental(Authentication authentication,
                                       @RequestBody RentalRequestDto requestDto) {
        return rentalService.addRental((User) authentication.getPrincipal(), requestDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Get all user's rentals",
            description = "Endpoint for getting all available user's rentals"
                    + " is still active or not")
    @ResponseStatus(HttpStatus.OK)
    List<RentalResponseDto> getRentalsById(@RequestParam(name = "user_id") Long userId,
                                       @RequestParam(name = "is_active") boolean active,
                                       Pageable pageable) {
        return rentalService.findRentalsById(userId, active, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Get specific rental by ID", description = "Get specific rental by ID")
    @ResponseStatus(HttpStatus.OK)
    public RentalResponseDto getRentalById(@PathVariable Long id) {
        return rentalService.findById(id);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Return the rental by ID", description = "Set actual return date by ID")
    @ResponseStatus(HttpStatus.OK)
    public RentalResponseDto setActualReturnDate(@PathVariable Long id) {
        return rentalService.setActualReturnDate(id);
    }
}
