package com.example.carsharingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingservice.dto.payment.PaymentRequestDto;
import com.example.carsharingservice.dto.payment.PaymentResponseDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.mapper.PaymentMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.payment.PaymentRepository;
import com.example.carsharingservice.repository.rental.RentalRepository;
import com.example.carsharingservice.service.notification.NotificationService;
import com.example.carsharingservice.service.payment.impl.PaymentServiceImpl;
import com.example.carsharingservice.service.stripe.StripeApiService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final String SESSION_ID = "sess_test_123";
    private static final String SESSION_URL = "https://checkout.stripe.com/pay/sess_test_123";

    @Mock
    private StripeApiService stripeApiService;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("createPayment: should create session, save payment and return DTO")
    void createPayment_validRequest_returnsPaymentResponseDto()
            throws StripeException {
        User user = buildUser(1L);
        Long rentalId = 1L;
        Car car = buildCar(1L, BigDecimal.valueOf(100));
        Rental rental = buildRental(rentalId, car, user,
                LocalDate.now(), LocalDate.now().plusDays(3), null);
        PaymentRequestDto requestDto =
                new PaymentRequestDto(rentalId, null, Payment.Type.PAYMENT);

        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getUrl()).thenReturn(SESSION_URL);

        Payment savedPayment = buildPayment(10L, rental, SESSION_ID, SESSION_URL,
                BigDecimal.valueOf(400), Payment.Status.PENDING, Payment.Type.PAYMENT);
        PaymentResponseDto expectedDto = new PaymentResponseDto(
                SESSION_ID, SESSION_URL, BigDecimal.valueOf(400), Payment.Status.PENDING);

        when(rentalRepository.findByIdAndUserId(rentalId, user.getId()))
                .thenReturn(Optional.of(rental));
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(stripeApiService.createPaymentSession(any())).thenReturn(session);
        when(paymentRepository.save(any())).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(expectedDto);

        PaymentResponseDto result = paymentService.createPayment(user, requestDto);

        assertThat(result).isEqualTo(expectedDto);
        verify(stripeApiService).createPaymentSession(any());
        verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("createPayment: should include fine amount when rental is overdue")
    void createPayment_overdueRental_includesFineInAmount()
            throws StripeException {
        User user = buildUser(1L);
        Long rentalId = 2L;
        Car car = buildCar(1L, BigDecimal.valueOf(50));

        Rental rental = buildRental(rentalId, car, user,
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(3),
                LocalDate.now().minusDays(1));
        PaymentRequestDto requestDto =
                new PaymentRequestDto(rentalId, null, Payment.Type.FINE);

        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getUrl()).thenReturn(SESSION_URL);

        Payment savedPayment = buildPayment(11L, rental, SESSION_ID, SESSION_URL,
                BigDecimal.valueOf(350), Payment.Status.PENDING, Payment.Type.FINE);
        PaymentResponseDto expectedDto = new PaymentResponseDto(
                SESSION_ID, SESSION_URL, BigDecimal.valueOf(350), Payment.Status.PENDING);

        when(rentalRepository.findByIdAndUserId(rentalId, user.getId()))
                .thenReturn(Optional.of(rental));
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(stripeApiService.createPaymentSession(any())).thenReturn(session);
        when(paymentRepository.save(any())).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(expectedDto);

        PaymentResponseDto result = paymentService.createPayment(user, requestDto);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("createPayment: throws EntityNotFoundException when rental not owned by user")
    void createPayment_rentalNotOwnedByUser_throwsEntityNotFoundException() throws StripeException {
        User user = buildUser(1L);
        Long rentalId = 99L;
        PaymentRequestDto requestDto =
                new PaymentRequestDto(rentalId, null, Payment.Type.PAYMENT);

        when(rentalRepository.findByIdAndUserId(rentalId, user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(user, requestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(rentalId));

        verify(stripeApiService, never()).createPaymentSession(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("findPaymentByUserId: should return list of payment DTOs for user")
    void findPaymentByUserId_existingUser_returnsPaymentDtoList() {
        Long userId = 1L;
        User user = buildUser(userId);
        Car car = buildCar(1L, BigDecimal.valueOf(100));
        Rental rental = buildRental(1L, car, user,
                LocalDate.now(), LocalDate.now().plusDays(3), null);

        Payment payment1 = buildPayment(1L, rental, "s1", "url1",
                BigDecimal.valueOf(300), Payment.Status.PAID, Payment.Type.PAYMENT);
        Payment payment2 = buildPayment(2L, rental, "s2", "url2",
                BigDecimal.valueOf(200), Payment.Status.PENDING, Payment.Type.PAYMENT);
        PaymentResponseDto dto1 = new PaymentResponseDto(
                "s1", "url1", BigDecimal.valueOf(300), Payment.Status.PAID);
        PaymentResponseDto dto2 = new PaymentResponseDto(
                "s2", "url2", BigDecimal.valueOf(200), Payment.Status.PENDING);

        when(paymentRepository.findByRentalId_User_Id(userId))
                .thenReturn(List.of(payment1, payment2));
        when(paymentMapper.toDto(payment1)).thenReturn(dto1);
        when(paymentMapper.toDto(payment2)).thenReturn(dto2);

        List<PaymentResponseDto> result = paymentService.findPaymentByUserId(userId);

        assertThat(result).hasSize(2).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("findPaymentByUserId: should return empty list when user has no payments")
    void findPaymentByUserId_noPayments_returnsEmptyList() {
        Long userId = 1L;
        when(paymentRepository.findByRentalId_User_Id(userId)).thenReturn(List.of());

        List<PaymentResponseDto> result = paymentService.findPaymentByUserId(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("checkSuccessfulPayments: should set status to PAID and return DTO")
    void checkSuccessfulPayments_validSessionId_setsStatusPaidAndReturnsDto() {
        User user = buildUser(1L);
        Car car = buildCar(1L, BigDecimal.valueOf(100));
        Rental rental = buildRental(1L, car, user,
                LocalDate.now(), LocalDate.now().plusDays(3), null);

        Payment payment = buildPayment(1L, rental, SESSION_ID, SESSION_URL,
                BigDecimal.valueOf(300), Payment.Status.PENDING, Payment.Type.PAYMENT);
        PaymentResponseDto expectedDto = new PaymentResponseDto(
                SESSION_ID, SESSION_URL, BigDecimal.valueOf(300), Payment.Status.PAID);

        when(paymentRepository.findBySessionId(SESSION_ID)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expectedDto);

        PaymentResponseDto result = paymentService.checkSuccessfulPayments(SESSION_ID);

        assertThat(payment.getStatus()).isEqualTo(Payment.Status.PAID);
        verify(paymentRepository).save(payment);
        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("cancelPayments: should set status to CANCELED and save")
    void cancelPayments_validSessionId_setsStatusCanceledAndSaves() {
        User user = buildUser(1L);
        Car car = buildCar(1L, BigDecimal.valueOf(100));
        Rental rental = buildRental(1L, car, user,
                LocalDate.now(), LocalDate.now().plusDays(3), null);

        Payment payment = buildPayment(1L, rental, SESSION_ID, SESSION_URL,
                BigDecimal.valueOf(300), Payment.Status.PENDING, Payment.Type.PAYMENT);

        when(paymentRepository.findBySessionId(SESSION_ID)).thenReturn(payment);

        paymentService.cancelPayments(SESSION_ID);

        assertThat(payment.getStatus()).isEqualTo(Payment.Status.CANCELED);
        verify(paymentRepository).save(payment);
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

    private Car buildCar(Long id, BigDecimal dailyFee) {
        Car car = new Car();
        car.setId(id);
        car.setModel("Model S");
        car.setBrand("Tesla");
        car.setType(Car.Type.SEDAN);
        car.setInventory(3);
        car.setDailyFee(dailyFee);
        return car;
    }

    private Rental buildRental(Long id, Car car, User user,
                                LocalDate rentalDate, LocalDate returnDate,
                                LocalDate actualReturnDate) {
        Rental rental = new Rental();
        rental.setId(id);
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDate(rentalDate);
        rental.setReturnDate(returnDate);
        rental.setActualReturnDate(actualReturnDate);
        rental.setActive(actualReturnDate == null);
        return rental;
    }

    private Payment buildPayment(Long id, Rental rental, String sessionId,
                                  String sessionUrl, BigDecimal amountToPay,
                                  Payment.Status status, Payment.Type type) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setRentalId(rental);
        payment.setSessionId(sessionId);
        payment.setSessionUrl(sessionUrl);
        payment.setAmountToPay(amountToPay);
        payment.setStatus(status);
        payment.setType(type);
        return payment;
    }
}
