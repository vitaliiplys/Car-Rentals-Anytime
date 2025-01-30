package com.example.carsharingservice.service.car.impl;

import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.mapper.CarMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.repository.car.CarRepository;
import com.example.carsharingservice.service.car.CarService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarMapper carMapper;
    private final CarRepository carRepository;

    @Override
    public CarResponseDto addCar(CarRequestDto requestDto) {
        return carMapper.toDto(carRepository.save(carMapper.toModel(requestDto)));
    }

    @Override
    public List<CarResponseDto> getAllCars(Pageable pageable) {
        return carRepository.findAll(pageable)
                .stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    public CarResponseDto getCarById(Long id) {
        Car carByID = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find car by id " + id)
        );
        return carMapper.toDto(carByID);
    }

    @Override
    public void updateCar(Long id, CarRequestDto requestDto) {
        Car carByID = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t update car inventory by id " + id));
        carByID.setInventory(requestDto.inventory());
        carRepository.save(carByID);
    }

    @Override
    public void deleteById(Long id) {
        if (carRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Can`t find car by id " + id);
        }
        carRepository.deleteById(id);
    }
}
