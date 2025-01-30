package com.example.carsharingservice.service.car;

import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto addCar(CarRequestDto requestDto);

    List<CarResponseDto> getAllCars(Pageable pageable);

    CarResponseDto getCarById(Long id);

    void deleteById(Long id);

    void updateCar(Long id, CarRequestDto requestDto);
}
