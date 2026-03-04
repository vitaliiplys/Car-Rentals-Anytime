package com.example.carsharingservice.service.stripe.imp;

import com.example.carsharingservice.dto.payment.PaymentRequestDto;
import com.example.carsharingservice.service.stripe.StripeApiService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeApiServiceImp implements StripeApiService {
    private static final String SUCCESS_URL = "http://localhost:8080/api/payments/success/{CHECKOUT_SESSION_ID}";
    private static final String CANCEL_URL = "http://localhost:8080/api/payments/cancel/{CHECKOUT_SESSION_ID}";
    private static final Long DEFAULT_QUANTITY_CAR = 1L;
    private static final String USD_CURRENCY = "usd";

    public Session createPaymentSession(PaymentRequestDto requestDto) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(SUCCESS_URL)
                .setCancelUrl(CANCEL_URL)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(DEFAULT_QUANTITY_CAR)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(USD_CURRENCY)
                                .setUnitAmount(requestDto.getAmountToPay()
                                        .multiply(BigDecimal.valueOf(100)).longValue())
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Car Rental Payment")
                                        .build())
                                .build())
                        .build())
                .build();
        return Session.create(params);
    }
}
