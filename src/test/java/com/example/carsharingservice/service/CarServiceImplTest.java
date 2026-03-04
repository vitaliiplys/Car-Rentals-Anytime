package com.example.carsharingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.mapper.CarMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.repository.car.CarRepository;
import com.example.carsharingservice.service.car.impl.CarServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock
    private CarMapper carMapper;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("addCar: should save car and return DTO")
    void addCar_validRequest_returnsSavedCarDto() {
        CarRequestDto requestDto = new CarRequestDto(
                "Model S", "Tesla", "SEDAN", 5, BigDecimal.valueOf(150)
        );

        Car carModel = buildCar(null, "Model S", "Tesla", Car.Type.SEDAN, 5,
                BigDecimal.valueOf(150));
        Car savedCar = buildCar(1L, "Model S", "Tesla", Car.Type.SEDAN, 5,
                BigDecimal.valueOf(150));
        CarResponseDto expectedDto = buildResponseDto(savedCar);

        when(carMapper.toModel(requestDto)).thenReturn(carModel);
        when(carRepository.save(carModel)).thenReturn(savedCar);
        when(carMapper.toDto(savedCar)).thenReturn(expectedDto);

        CarResponseDto result = carService.addCar(requestDto);

        assertThat(result).isEqualTo(expectedDto);
        verify(carRepository).save(carModel);
    }

    @Test
    @DisplayName("getAllCars: should return list of car DTOs")
    void getAllCars_existingCars_returnsCarDtoList() {
        Pageable pageable = PageRequest.of(0, 10);

        Car car1 = buildCar(1L, "Model S", "Tesla", Car.Type.SEDAN, 3, BigDecimal.valueOf(100));
        Car car2 = buildCar(2L, "X5", "BMW", Car.Type.SUV, 2, BigDecimal.valueOf(200));
        Page<Car> page = new PageImpl<>(List.of(car1, car2));

        CarResponseDto dto1 = buildResponseDto(car1);
        CarResponseDto dto2 = buildResponseDto(car2);

        when(carRepository.findAll(pageable)).thenReturn(page);
        when(carMapper.toDto(car1)).thenReturn(dto1);
        when(carMapper.toDto(car2)).thenReturn(dto2);

        List<CarResponseDto> result = carService.getAllCars(pageable);

        assertThat(result).hasSize(2).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("getAllCars: should return empty list when no cars exist")
    void getAllCars_noCars_returnsEmptyList() {
        Pageable pageable = PageRequest.of(0, 10);
        when(carRepository.findAll(pageable)).thenReturn(Page.empty());

        List<CarResponseDto> result = carService.getAllCars(pageable);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCarById: should return car DTO when car exists")
    void getCarById_existingId_returnsCarDto() {
        Long id = 1L;
        Car car = buildCar(id, "Civic", "Honda", Car.Type.SEDAN, 4, BigDecimal.valueOf(50));
        CarResponseDto expectedDto = buildResponseDto(car);

        when(carRepository.findById(id)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expectedDto);

        CarResponseDto result = carService.getCarById(id);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("getCarById: should throw EntityNotFoundException when car not found")
    void getCarById_nonExistingId_throwsEntityNotFoundException() {
        Long id = 99L;
        when(carRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.getCarById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(id));
    }

    @Test
    @DisplayName("updateCar: should update inventory and save car")
    void updateCar_existingId_updatesInventoryAndSaves() {
        Long id = 1L;
        int newInventory = 10;
        CarRequestDto requestDto = new CarRequestDto(
                "Civic", "Honda", "SEDAN", newInventory, BigDecimal.valueOf(50)
        );
        Car car = buildCar(id, "Civic", "Honda", Car.Type.SEDAN, 4, BigDecimal.valueOf(50));

        when(carRepository.findById(id)).thenReturn(Optional.of(car));

        carService.updateCar(id, requestDto);

        assertThat(car.getInventory()).isEqualTo(newInventory);
        verify(carRepository).save(car);
    }

    @Test
    @DisplayName("updateCar: should throw EntityNotFoundException when car not found")
    void updateCar_nonExistingId_throwsEntityNotFoundException() {
        Long id = 99L;
        CarRequestDto requestDto = new CarRequestDto(
                "Civic", "Honda", "SEDAN", 5, BigDecimal.valueOf(50)
        );
        when(carRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.updateCar(id, requestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(id));
    }

    @Test
    @DisplayName("deleteById: should delete car when it exists")
    void deleteById_existingId_deletesSuccessfully() {
        Long id = 1L;
        Car car = buildCar(id, "Civic", "Honda", Car.Type.SEDAN, 4, BigDecimal.valueOf(50));

        when(carRepository.findById(id)).thenReturn(Optional.of(car));

        carService.deleteById(id);

        verify(carRepository).deleteById(id);
    }

    @Test
    @DisplayName("deleteById: should throw EntityNotFoundException when car not found")
    void deleteById_nonExistingId_throwsEntityNotFoundException() {
        Long id = 99L;
        when(carRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.deleteById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(id));
    }

    private Car buildCar(Long id, String model, String brand,
                         Car.Type type, int inventory, BigDecimal dailyFee) {
        Car car = new Car();
        car.setId(id);
        car.setModel(model);
        car.setBrand(brand);
        car.setType(type);
        car.setInventory(inventory);
        car.setDailyFee(dailyFee);
        return car;
    }

    private CarResponseDto buildResponseDto(Car car) {
        return new CarResponseDto(
                car.getId(),
                car.getModel(),
                car.getBrand(),
                car.getType(),
                car.getInventory(),
                car.getDailyFee()
        );
    }
}
