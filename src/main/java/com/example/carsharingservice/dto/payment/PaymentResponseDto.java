package com.example.carsharingservice.dto.payment;

import com.example.carsharingservice.model.Payment;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@AllArgsConstructor
public class PaymentResponseDto {
    private String sessionId;
    private String sessionUrl;
    private BigDecimal amountToPay;
    private Payment.Status status;
}
