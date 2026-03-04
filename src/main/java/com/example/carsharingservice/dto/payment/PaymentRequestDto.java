package com.example.carsharingservice.dto.payment;

import com.example.carsharingservice.model.Payment;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@AllArgsConstructor
public class PaymentRequestDto {
    private Long rentalId;
    private BigDecimal amountToPay;
    private Payment.Type type;
}
