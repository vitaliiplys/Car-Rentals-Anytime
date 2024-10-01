package com.example.carsharingservice.service.rental.impl;

import com.example.carsharingservice.dto.rental.RentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.example.carsharingservice.exception.CarRentalException;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.mapper.RentalMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.car.CarRepository;
import com.example.carsharingservice.repository.rental.RentalRepository;
import com.example.carsharingservice.repository.user.UserRepository;
import com.example.carsharingservice.service.rental.RentalService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private static final int ZERO_INVENTORY = 0;
    private static final int DECREASE_BY_ONE = 1;
    private static final int INCREASE_BY_ONE = 1;

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final UserRepository userRepository;

    @Override
    public RentalResponseDto addRental(User principal, RentalRequestDto requestDto) {
        if (requestDto.rentalDate() == null || requestDto.returnDate() == null) {
            throw new CarRentalException("Rental date cannot be empty");
        }

        Car carFromDb = carRepository.findById(requestDto.carId()).orElseThrow(
                () -> new EntityNotFoundException("Car not found " + requestDto.carId())
        );

        if (carFromDb.getInventory() <= ZERO_INVENTORY) {
            throw new CarRentalException(
                    "Sorry this car with id " + requestDto.carId() + " is not available now"
            );
        }

        carFromDb.setInventory(carFromDb.getInventory() - DECREASE_BY_ONE);
        carRepository.save(carFromDb);
        Rental newRental = rentalMapper.toModel(requestDto);
        newRental.setUserId(principal.getId());
        Rental savedRental = rentalRepository.save(newRental);
        return rentalMapper.toDto(savedRental);
    }

    @Override
    public List<RentalResponseDto> findRentalsById(Long userId, boolean active, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found " + userId);
        }
        Page<Rental> allByUserIdAndActive =
                rentalRepository.findAllByUserIdAndActive(userId, active, pageable);
        if (allByUserIdAndActive.isEmpty()) {
            throw new EntityNotFoundException("User not found " + userId);
        }
        return allByUserIdAndActive.stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public RentalResponseDto findById(Long id) {
        Rental rentalByID = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Rental not found " + id));
        return rentalMapper.toDto(rentalByID);
    }

    @Override
    public RentalResponseDto setActualReturnDate(Long id) {
        Rental rentalByID = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Rental not found " + id));
        rentalByID.setActualReturnDate(LocalDate.now());
        rentalByID.setActive(false);
        rentalRepository.save(rentalByID);

        Car carFromDB = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Car not found " + id));
        carFromDB.setInventory(carFromDB.getInventory() + INCREASE_BY_ONE);
        carRepository.save(carFromDB);
        return rentalMapper.toDto(rentalByID);
    }
}
