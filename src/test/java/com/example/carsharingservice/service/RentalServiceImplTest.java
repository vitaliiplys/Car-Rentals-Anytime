package com.example.carsharingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.carsharingservice.service.notification.NotificationService;
import com.example.carsharingservice.service.rental.impl.RentalServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class RentalServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("addRental: should create rental and decrease car inventory")
    void addRental_validRequest_savesRentalAndDecreasesInventory() {
        User principal = buildUser(1L);
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDate.now(), LocalDate.now().plusDays(3), null, 1L, 1L
        );

        Car car = buildCar(1L, 3);
        Rental rentalModel = buildRental(null, car, principal, true);
        Rental savedRental = buildRental(10L, car, principal, true);
        RentalResponseDto expectedDto = buildResponseDto(savedRental);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(rentalMapper.toModel(requestDto)).thenReturn(rentalModel);
        when(rentalRepository.save(rentalModel)).thenReturn(savedRental);
        when(rentalMapper.toDto(savedRental)).thenReturn(expectedDto);

        RentalResponseDto result = rentalService.addRental(principal, requestDto);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(car.getInventory()).isEqualTo(2);
        verify(carRepository).save(car);
        verify(rentalRepository).save(rentalModel);
    }

    @Test
    @DisplayName("addRental: should throw CarRentalException when rentalDate is null")
    void addRental_nullRentalDate_throwsCarRentalException() {
        User principal = buildUser(1L);
        RentalRequestDto requestDto = new RentalRequestDto(
                null, LocalDate.now().plusDays(3), null, 1L, 1L
        );

        assertThatThrownBy(() -> rentalService.addRental(principal, requestDto))
                .isInstanceOf(CarRentalException.class)
                .hasMessageContaining("Rental date cannot be empty");

        verify(carRepository, never()).findById(any());
    }

    @Test
    @DisplayName("addRental: should throw CarRentalException when returnDate is null")
    void addRental_nullReturnDate_throwsCarRentalException() {
        User principal = buildUser(1L);
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDate.now(), null, null, 1L, 1L
        );

        assertThatThrownBy(() -> rentalService.addRental(principal, requestDto))
                .isInstanceOf(CarRentalException.class)
                .hasMessageContaining("Rental date cannot be empty");

        verify(carRepository, never()).findById(any());
    }

    @Test
    @DisplayName("addRental: should throw EntityNotFoundException when car not found")
    void addRental_carNotFound_throwsEntityNotFoundException() {
        User principal = buildUser(1L);
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDate.now(), LocalDate.now().plusDays(3), null, 99L, 1L
        );

        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.addRental(principal, requestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("addRental: should throw CarRentalException when car inventory is zero")
    void addRental_carInventoryZero_throwsCarRentalException() {
        User principal = buildUser(1L);
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDate.now(), LocalDate.now().plusDays(3), null, 1L, 1L
        );
        Car car = buildCar(1L, 0);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        assertThatThrownBy(() -> rentalService.addRental(principal, requestDto))
                .isInstanceOf(CarRentalException.class)
                .hasMessageContaining("1");

        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("findRentalsById: should return active rentals for existing user")
    void findRentalsById_existingUserActiveRentals_returnsDtoList() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        User user = buildUser(userId);
        Car car = buildCar(1L, 2);

        Rental rental = buildRental(1L, car, user, true);
        RentalResponseDto dto = buildResponseDto(rental);
        Page<Rental> page = new PageImpl<>(List.of(rental));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(rentalRepository.findAllByUserIdAndActive(userId, true, pageable)).thenReturn(page);
        when(rentalMapper.toDto(rental)).thenReturn(dto);

        List<RentalResponseDto> result = rentalService.findRentalsById(userId, true, pageable);

        assertThat(result).hasSize(1).containsExactly(dto);
    }

    @Test
    @DisplayName("findRentalsById: should return inactive rentals for existing user")
    void findRentalsById_existingUserInactiveRentals_returnsDtoList() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        User user = buildUser(userId);
        Car car = buildCar(1L, 2);

        Rental rental = buildRental(2L, car, user, false);
        RentalResponseDto dto = buildResponseDto(rental);
        Page<Rental> page = new PageImpl<>(List.of(rental));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(rentalRepository.findAllByUserIdAndActive(userId, false, pageable)).thenReturn(page);
        when(rentalMapper.toDto(rental)).thenReturn(dto);

        List<RentalResponseDto> result = rentalService.findRentalsById(userId, false, pageable);

        assertThat(result).hasSize(1).containsExactly(dto);
    }

    @Test
    @DisplayName("findRentalsById: should throw EntityNotFoundException when user not found")
    void findRentalsById_userNotFound_throwsEntityNotFoundException() {
        Long userId = 99L;
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> rentalService.findRentalsById(userId, true, pageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(rentalRepository, never()).findAllByUserIdAndActive(any(), anyBoolean(), any());
    }

    @Test
    @DisplayName("findRentalsById: should throw EntityNotFoundException when no rentals found")
    void findRentalsById_noRentalsFound_throwsEntityNotFoundException() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(rentalRepository.findAllByUserIdAndActive(userId, true, pageable))
                .thenReturn(Page.empty());

        assertThatThrownBy(() -> rentalService.findRentalsById(userId, true, pageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(userId));
    }

    @Test
    @DisplayName("findById: should return rental DTO when rental exists")
    void findById_existingId_returnsRentalDto() {
        Long id = 1L;
        User user = buildUser(1L);
        Car car = buildCar(1L, 2);
        Rental rental = buildRental(id, car, user, true);
        RentalResponseDto expectedDto = buildResponseDto(rental);

        when(rentalRepository.findById(id)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        RentalResponseDto result = rentalService.findById(id);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("findById: should throw EntityNotFoundException when rental not found")
    void findById_nonExistingId_throwsEntityNotFoundException() {
        Long id = 99L;
        when(rentalRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("setActualReturnDate: should set return date, deactivate and increase inventory")
    void setActualReturnDate_existingRental_updatesRentalAndCar() {
        Long id = 1L;
        User user = buildUser(1L);
        Car car = buildCar(1L, 1);
        Rental rental = buildRental(id, car, user, true);
        RentalResponseDto expectedDto = new RentalResponseDto(
                id, rental.getRentalDate(), LocalDate.now(), car.getId(), user.getId(), false
        );

        when(rentalRepository.findById(id)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        RentalResponseDto result = rentalService.setActualReturnDate(id);

        assertThat(rental.getActualReturnDate()).isEqualTo(LocalDate.now());
        assertThat(rental.isActive()).isFalse();
        assertThat(car.getInventory()).isEqualTo(2);
        verify(rentalRepository).save(rental);
        verify(carRepository).save(car);
        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("setActualReturnDate: should throw EntityNotFoundException when rental not found")
    void setActualReturnDate_nonExistingId_throwsEntityNotFoundException() {
        Long id = 99L;
        when(rentalRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.setActualReturnDate(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(rentalRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@test.com");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setPassword("password");
        return user;
    }

    private Car buildCar(Long id, int inventory) {
        Car car = new Car();
        car.setId(id);
        car.setModel("Model S");
        car.setBrand("Tesla");
        car.setType(Car.Type.SEDAN);
        car.setInventory(inventory);
        car.setDailyFee(BigDecimal.valueOf(100));
        return car;
    }

    private Rental buildRental(Long id, Car car, User user, boolean active) {
        Rental rental = new Rental();
        rental.setId(id);
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(3));
        rental.setActive(active);
        return rental;
    }

    private RentalResponseDto buildResponseDto(Rental rental) {
        return new RentalResponseDto(
                rental.getId(),
                rental.getRentalDate(),
                rental.getActualReturnDate(),
                rental.getCar().getId(),
                rental.getUser().getId(),
                rental.isActive()
        );
    }
}
