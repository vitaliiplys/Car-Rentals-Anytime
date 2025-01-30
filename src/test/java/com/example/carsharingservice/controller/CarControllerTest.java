package com.example.carsharingservice.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingservice.dto.car.CarRequestDto;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.model.Car;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/01-insert-manager.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/02-insert-manager-role.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/04-inserts-two-default-cars.sql")
            );
        }
    }

    @AfterEach
    void afterEach(
            @Autowired DataSource dataSource
    ) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/07-teardown-all.sql")
            );
        }
    }

    @WithMockUser(username = "Admin", roles = {"MANAGER"})
    @Test
    @Sql(
            scripts = "classpath:database/03-delete-car-BMW.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Create a new car valid")
    void createCar_Valid_ShouldReturnSuccess() throws Exception {
        // Given
        CarRequestDto requestDto = new CarRequestDto(
                "M5", "BMW", "SEDAN", 1, BigDecimal.valueOf(1000.00));

        CarResponseDto expected = new CarResponseDto(
                1L, "M5", "BMW", Car.Type.SEDAN, 1, BigDecimal.valueOf(1000.00));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/cars")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        CarResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CarResponseDto.class);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @WithMockUser(username = "Admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Get all cars valid")
    void getAllCars_Valid_ShouldReturnSuccess() throws Exception {
        // Given
        List<CarResponseDto> expected = createCarResponseDto();

        // When
        MvcResult result = mockMvc.perform(get("/cars")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<CarResponseDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<List<CarResponseDto>>() {
                });
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "Admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Get car by id valid")
    void getCar_ByValid_ShouldReturnSuccess() throws Exception {
        // Given
        Long carId = 2L;

        CarResponseDto expected = new CarResponseDto(
                carId,
                "Model S",
                "Tesla",
                Car.Type.SEDAN,
                1,
                BigDecimal.valueOf(1100.99));

        // When
        MvcResult result = mockMvc.perform(get("/cars/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        CarResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CarResponseDto.class);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @WithMockUser(username = "Admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Get car by id invalid")
    void getCar_ByInvalid_ShouldReturnException() throws Exception {
        // Given
        Long carInvalid = -100L;

        // When
        MvcResult result = mockMvc.perform(get("/cars/{carInvalid}", carInvalid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can`t find car by id " + carInvalid));
    }

    @Sql(
            scripts = "classpath:database/05-restore-car-after-update.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @WithMockUser(username = "Admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Update car by id")
    void updateCar_Valid_ShouldReturnSuccess() throws Exception {
        // Given
        Long carId = 2L;

        CarRequestDto updateRequestDto = createUpdateInventoryCarRequestDto();

        CarResponseDto expected = new CarResponseDto(
                carId,
                updateRequestDto.model(),
                updateRequestDto.brand(),
                Car.Type.valueOf(updateRequestDto.type()),
                updateRequestDto.inventory(),
                updateRequestDto.dailyFee()
        );

        String jsonRequest = objectMapper.writeValueAsString(updateRequestDto);

        // When
        MvcResult result = mockMvc.perform(put("/cars/{carId}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result1 = mockMvc.perform(get("/cars/{carId}", carId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CarResponseDto actual = objectMapper.readValue(result1.getResponse()
                .getContentAsString(), CarResponseDto.class);
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "Admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Update car by invalid id")
    void update_Car_Invalid_Id_ShouldReturnException() throws Exception {
        // When
        Long carId = -100L;

        CarRequestDto invalidUpdateRequestDto = new CarRequestDto(
                "Model S",
                "Tesla",
                "SEDAN",
                1,
                BigDecimal.valueOf(1100.99));

        String jsonRequest = objectMapper.writeValueAsString(invalidUpdateRequestDto);

        // When
        MvcResult result = mockMvc.perform(put("/cars/{carId}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can`t update car inventory by id " + carId));
    }

    @WithMockUser(username = "Admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Delete car by valid id")
    void deleteCar_Valid_ShouldReturnSuccess() throws Exception {
        // Given
        Long carValidId = 3L;

        // When
        MvcResult result = mockMvc.perform(delete("/cars/{carValidId}", carValidId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    @WithMockUser(username = "Admin", roles = {"MANAGER"})
    @Test
    @DisplayName("Delete car by invalid id")
    void deleteCar_Invalid_Id_ShouldReturnException() throws Exception {
        // Given
        Long carInvalidId = -10L;

        // When
        MvcResult result = mockMvc.perform(delete("/cars/{carId}", carInvalidId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can`t find car by id " + carInvalidId));
    }

    private CarRequestDto createUpdateInventoryCarRequestDto(
    ) {
        return new CarRequestDto(
                "Model S",
                "Tesla",
                "SEDAN",
                6,
                BigDecimal.valueOf(1100.99));
    }

    private List<CarResponseDto> createCarResponseDto() {
        List<CarResponseDto> carResponseDtoList = new ArrayList<>();
        carResponseDtoList.add(new CarResponseDto(
                2L,
                "Model S",
                "Tesla",
                Car.Type.SEDAN,
                1,
                BigDecimal.valueOf(1100.99)));
        carResponseDtoList.add(new CarResponseDto(
                3L,
                "Model Y",
                "Tesla",
                Car.Type.SUV,
                1,
                BigDecimal.valueOf(1500.99)));
        return carResponseDtoList;
    }
}
