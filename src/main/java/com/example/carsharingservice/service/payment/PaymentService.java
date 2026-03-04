package com.example.carsharingservice.service.payment;

import com.example.carsharingservice.dto.payment.PaymentRequestDto;
import com.example.carsharingservice.dto.payment.PaymentResponseDto;
import com.example.carsharingservice.model.User;
import com.stripe.exception.StripeException;
import java.util.List;

public interface PaymentService {
    PaymentResponseDto createPayment(User user, PaymentRequestDto requestDto)
            throws StripeException;

    List<PaymentResponseDto> findPaymentByUserId(Long userId);

    PaymentResponseDto checkSuccessfulPayments(String sessionId);

    void cancelPayments(String sessionId);
}
