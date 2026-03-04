package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.payment.PaymentRequestDto;
import com.example.carsharingservice.dto.payment.PaymentResponseDto;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.service.payment.PaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments controller", description = "Endpoints for payments cars")
@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentsController {
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create payment session",
            description = "Initialize session and create payment")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDto createPayment(@RequestBody PaymentRequestDto requestDto,
                                            Authentication authentication) throws StripeException {
        return paymentService.createPayment((User) authentication.getPrincipal(), requestDto);
    }

    @Operation(summary = "Get all available payment by user id",
            description = "Endpoint get a list of all payment by user id")
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public List<PaymentResponseDto> getPaymentByUserId(@PathVariable Long userId) {
        return paymentService.findPaymentByUserId(userId);
    }

    @Operation(summary = "Success session",
            description = "Payment successfully processed.")

    @GetMapping("/success{sessionId}")
    @ResponseStatus(HttpStatus.OK)
    public PaymentResponseDto checkSuccessfulPayments(@RequestParam String sessionId) {
        return paymentService.checkSuccessfulPayments(sessionId);
    }

    @Operation(summary = "Cancel session",
            description = "Payment session expired or canceled.")
    @GetMapping("/cancel{sessionId}")
    @ResponseStatus(HttpStatus.OK)
    public String cancelPayments(@RequestParam String sessionId) {
        paymentService.cancelPayments(sessionId);
        return "Payment was cancelled."
                + "The session is available for the next 24 hours";
    }
}
