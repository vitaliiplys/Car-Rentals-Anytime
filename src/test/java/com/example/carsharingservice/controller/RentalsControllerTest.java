package com.example.carsharingservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingservice.dto.rental.RentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
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

@Sql(scripts = {"classpath:database/07-teardown-all.sql",
        "classpath:database/01-insert-manager.sql",
        "classpath:database/02-insert-manager-role.sql",
        "classpath:database/04-inserts-two-default-cars.sql",
        "classpath:database/06-insert-rentals.sql",
})

@Sql(scripts = "classpath:database/07-teardown-all.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RentalsControllerTest {
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
    @DisplayName("Create a new rental valid")
    void createRental_Valid_ShouldReturnSuccess() throws Exception {
        // Given
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 7),
                LocalDate.of(2025, 1, 9),
                2L,
                1L
        );

        RentalResponseDto expected = new RentalResponseDto(
                1L,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 9),
                2L,
                1L,
                true
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        RentalResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), RentalResponseDto.class);
        Assertions.assertEquals(expected, actual);
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Create a new rental invalid data rentalData null")
    void createRental_InvalidDataReturn_ShouldReturnBadRequest() throws Exception {
        // Given
        RentalRequestDto requestDto = new RentalRequestDto(
                null,
                LocalDate.of(2025, 1, 7),
                LocalDate.of(2025, 1, 9),
                2L,
                1L
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Rental date cannot be empty"));
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Create a new rental invalid data returnDate null")
    void createRental_InvalidData_RentalDate_ShouldReturnBadRequest() throws Exception {
        // Given
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDate.of(2025, 1, 1),
                null,
                LocalDate.of(2025, 1, 9),
                2L,
                1L
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Rental date cannot be empty"));
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Create a new rental invalid data returnDate null")
    void createRental_InvalidData_ReturnDate_ShouldReturnBadRequest() throws Exception {
        // Given
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDate.of(2025, 1, 1),
                null,
                LocalDate.of(2025, 1, 9),
                2L,
                1L
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Rental date cannot be empty"));
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Create a new rental invalid car id")
    void createRental_InvalidCarId_ShouldReturnBadRequest() throws Exception {
        // Given
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 7),
                LocalDate.of(2025, 1, 9),
                999L,
                1L
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Car not found " + requestDto.carId()));
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Getting all available user's rentals")
    void getRentals_ByUserValidIdAndActive_ShouldReturnSuccess() throws Exception {
        // Given
        Long userId = 1L;
        boolean active = true;

        List<RentalResponseDto> expected = createResponseDtoList();

        // When
        MvcResult result = mockMvc.perform(get("/rentals")
                        .param("user_id", String.valueOf(userId))
                        .param("is_active", String.valueOf(active))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<RentalResponseDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<List<RentalResponseDto>>() {
                });
        Assertions.assertEquals(expected, actual);
    }

    @WithUserDetails("admin@example.com")
    @DisplayName("Getting all available user's rentals invalid user id")
    @Test
    void getRentals_ByInvalidUserId_ShouldReturnNotFound() throws Exception {
        // Given
        Long userId = 999L;
        boolean active = true;

        // When
        MvcResult result = mockMvc.perform(get("/rentals")
                        .param("user_id", String.valueOf(userId))
                        .param("is_active", String.valueOf(active))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("User not found " + userId));
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Get rentals by valid id")
    void getRentals_ByValidId_ShouldReturnSuccess() throws Exception {
        // When
        Long rentalId = 2L;

        RentalResponseDto expected = new RentalResponseDto(
                rentalId,
                LocalDate.of(2025,1,1),
                LocalDate.of(2025,1,9),
                2L,
                1L,
                true);

        // When
        MvcResult result = mockMvc.perform(
                        get("/rentals/{id}", rentalId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        RentalResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), RentalResponseDto.class);
        Assertions.assertEquals(expected, actual);
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Find rentals by id user is not found")
    void findRentals_UserNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        Long rentalId = 999L;

        // When
        MvcResult result = mockMvc.perform(get("/rentals/{id}", rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Rental not found " + rentalId));
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Set actual return date by invalid id")
    void setRentals_InvalidId_ShouldReturnBadRequest() throws Exception {
        // Given
        Long rentalId = 77L;

        // When
        MvcResult result = mockMvc.perform(post("/rentals/{id}/return", rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Rental not found " + rentalId));
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Set actual return date by invalid car id")
    void setRentals_InvalidCarId_ShouldReturnBadRequest() throws Exception {
        // Given
        Long id = 2L;

        RentalResponseDto expected = new RentalResponseDto(2L,
                LocalDate.of(2025, 1, 1),
                LocalDate.now(),
                2L,
                1L,
                false);

        // When
        MvcResult result = mockMvc.perform(post("/rentals/{id}/return", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        RentalResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), RentalResponseDto.class);
        Assertions.assertEquals(expected, actual);
    }

    private List<RentalResponseDto> createResponseDtoList() {
        List<RentalResponseDto> rentalResponseDto = new ArrayList<>();
        rentalResponseDto.add(new RentalResponseDto(2L,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 9),
                2L,
                1L,
                true));
        rentalResponseDto.add(new RentalResponseDto(3L,
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 10),
                3L,
                1L,
                true));
        return rentalResponseDto;
    }
}
