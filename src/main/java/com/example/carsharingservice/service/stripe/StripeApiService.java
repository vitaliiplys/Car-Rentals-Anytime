package com.example.carsharingservice.service.stripe;

import com.example.carsharingservice.dto.payment.PaymentRequestDto;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

public interface StripeApiService {
    Session createPaymentSession(PaymentRequestDto requestDto)
            throws StripeException;
}
