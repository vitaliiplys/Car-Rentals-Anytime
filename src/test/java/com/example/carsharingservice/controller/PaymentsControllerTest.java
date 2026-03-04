package com.example.carsharingservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingservice.dto.payment.PaymentRequestDto;
import com.example.carsharingservice.dto.payment.PaymentResponseDto;
import com.example.carsharingservice.model.Payment;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Sql(scripts = {
        "classpath:database/13-teardown-all.sql",
        "classpath:database/01-insert-manager.sql",
        "classpath:database/02-insert-manager-role.sql",
        "classpath:database/04-inserts-two-default-cars.sql",
        "classpath:database/10-insert-rentals.sql",
        "classpath:database/15-insert-payments.sql"
})
@Sql(scripts = "classpath:database/13-teardown-all.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentsControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Create payment - rental not found for user returns 404")
    void createPayment_RentalNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto(
                999L, BigDecimal.valueOf(100.00), Payment.Type.PAYMENT);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/payments")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Rental with ID 999 not found for user"));
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Get payments by user id - returns list of payments")
    void getPaymentByUserId_ValidId_ShouldReturnList() throws Exception {
        // Given
        Long userId = 1L;

        // When
        MvcResult result = mockMvc.perform(
                        get("/payments/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<PaymentResponseDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<List<PaymentResponseDto>>() {
                });
        Assertions.assertEquals(2, actual.size());
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Get payments by user id - no payments returns empty list")
    void getPaymentByUserId_NoPayments_ShouldReturnEmptyList() throws Exception {
        // Given
        Long userId = 999L;

        // When
        MvcResult result = mockMvc.perform(
                        get("/payments/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<PaymentResponseDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<List<PaymentResponseDto>>() {
                });
        Assertions.assertTrue(actual.isEmpty());
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Check successful payment - valid session id returns PAID status")
    void checkSuccessfulPayments_ValidSessionId_ShouldReturnPaidStatus() throws Exception {
        // Given
        String sessionId = "sess_pending123";

        // When
        MvcResult result = mockMvc.perform(
                        get("/payments/success")
                                .param("sessionId", sessionId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        PaymentResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), PaymentResponseDto.class);
        Assertions.assertEquals(Payment.Status.PAID, actual.getStatus());
        Assertions.assertEquals(sessionId, actual.getSessionId());
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Cancel payment - valid session id returns cancel message")
    void cancelPayments_ValidSessionId_ShouldReturnCancelMessage() throws Exception {
        // Given
        String sessionId = "sess_paid456";

        // When
        MvcResult result = mockMvc.perform(
                        get("/payments/cancel")
                                .param("sessionId", sessionId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Payment was cancelled."));
    }
}
