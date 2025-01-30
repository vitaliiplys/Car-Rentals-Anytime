package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.service.car.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cars controller", description = "Endpoints for managing cars")
@RequiredArgsConstructor
@RestController
@RequestMapping("cars")
public class CarController {
    private final CarService carService;

    @Operation(summary = "Create a new car",
            description = "Endpoint for creating and saving a new car to database."
                    + " Allowed for managers only")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping
    public CarResponseDto addCar(@RequestBody @Valid CarRequestDto requestDto) {
        return carService.addCar(requestDto);
    }

    @Operation(summary = "Get all available cars",
            description = "Endpoint for seeing all available cars with pageable sorting")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<CarResponseDto> getAllCars(Pageable pageable) {
        return carService.getAllCars(pageable);
    }

    @Operation(summary = "Get car by id", description = "Get car by id")
    @GetMapping("/{id}")
    public CarResponseDto getCarById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Update a car",
            description = "Endpoint for updating the pointed car.")
    public void updateCar(@PathVariable Long id,
                                    @RequestBody CarRequestDto requestDto) {
        carService.updateCar(id, requestDto);
    }

    @Operation(summary = "Delete a car by id", description = "Delete a car by id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/{id}")
    public void deleteCarById(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
