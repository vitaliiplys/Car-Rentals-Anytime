package com.example.carsharingservice.service.payment.impl;

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
import com.example.carsharingservice.service.payment.PaymentService;
import com.example.carsharingservice.service.stripe.StripeApiService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final StripeApiService stripeApiService;
    private final RentalRepository rentalRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    @Override
    public PaymentResponseDto createPayment(
            User user, PaymentRequestDto requestDto) throws StripeException {

        if (rentalRepository.findByIdAndUserId(requestDto.getRentalId(), user.getId()).isEmpty()) {
            throw new EntityNotFoundException(
                    "Rental with ID " + requestDto.getRentalId()
                            + " not found for user " + user.getId());
        }

        BigDecimal amountToPay = calculateFineAmount(requestDto.getRentalId())
                .add(calculateAmountToPay(requestDto.getRentalId()));

        Session paymentSession = stripeApiService.createPaymentSession(
                requestDto.setAmountToPay(amountToPay));

        PaymentResponseDto responseDto = new PaymentResponseDto(
                paymentSession.getId(),
                paymentSession.getUrl(),
                requestDto.getAmountToPay(),
                Payment.Status.PENDING);

        Payment payment = createPaymentEntity(requestDto, responseDto);

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public List<PaymentResponseDto> findPaymentByUserId(Long userId) {
        List<Payment> paymentsList = paymentRepository.findByRentalId_User_Id(userId);
        return paymentsList.stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public PaymentResponseDto checkSuccessfulPayments(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId);
        payment.setStatus(Payment.Status.PAID);
        paymentRepository.save(payment);
        notificationService.sendPaymentSuccessNotification(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    public void cancelPayments(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId);
        payment.setStatus(Payment.Status.CANCELED);
        paymentRepository.save(payment);
        paymentMapper.toDto(payment);
    }

    private Payment createPaymentEntity(
            PaymentRequestDto requestDto, PaymentResponseDto responseDto) {
        Payment payment = new Payment();
        payment.setRentalId(getRentalById(requestDto.getRentalId()));
        payment.setSessionId(responseDto.getSessionId());
        payment.setSessionUrl(responseDto.getSessionUrl());
        payment.setAmountToPay(responseDto.getAmountToPay());
        payment.setStatus(responseDto.getStatus());
        payment.setType(requestDto.getType());
        return payment;
    }

    private BigDecimal calculateAmountToPay(Long rentalId) {
        Rental rental = getRentalById(rentalId);
        Car rentalCar = rental.getCar();
        LocalDate checkInDate = rental.getRentalDate();
        LocalDate checkOutDate = rental.getReturnDate();

        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate) + 1;

        return rentalCar.getDailyFee().multiply(BigDecimal.valueOf(days));
    }

    private BigDecimal calculateFineAmount(Long rentalId) {
        Rental rental = getRentalById(rentalId);
        LocalDate returnDate = rental.getReturnDate();
        LocalDate actualReturnDate = rental.getActualReturnDate();

        int overdueDays;

        if (actualReturnDate != null) {
            overdueDays = (int) ChronoUnit.DAYS.between(returnDate, actualReturnDate);
        } else {
            overdueDays = (int) ChronoUnit.DAYS.between(returnDate, LocalDate.now()
            );
        }

        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal fineMultiplier = BigDecimal.valueOf(2);
        return rental.getCar().getDailyFee()
                .multiply(BigDecimal.valueOf(overdueDays))
                .multiply(fineMultiplier);
    }

    private Rental getRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Rental not found" + rentalId));
    }
}
