package com.example.carsharingservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingservice.dto.role.RoleRequestDto;
import com.example.carsharingservice.dto.user.UserResponseDto;
import com.example.carsharingservice.dto.user.UserUpdateProfileRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        "classpath:database/11-insert-customer.sql"
})
@Sql(scripts = "classpath:database/13-teardown-all.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
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
    @DisplayName("Get my profile - returns authenticated manager profile")
    void getMyProfile_AuthenticatedManager_ShouldReturnProfile() throws Exception {
        // When
        MvcResult result = mockMvc.perform(
                        get("/users/me")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        Assertions.assertEquals(1L, actual.id());
        Assertions.assertEquals("admin@example.com", actual.email());
        Assertions.assertEquals("Admin", actual.firstName());
        Assertions.assertEquals("Admin", actual.lastName());
    }

    @WithUserDetails("customer@example.com")
    @Test
    @DisplayName("Get my profile - returns authenticated customer profile")
    void getMyProfile_AuthenticatedCustomer_ShouldReturnProfile() throws Exception {
        // When
        MvcResult result = mockMvc.perform(
                        get("/users/me")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        Assertions.assertEquals(2L, actual.id());
        Assertions.assertEquals("customer@example.com", actual.email());
        Assertions.assertEquals("Customer", actual.firstName());
        Assertions.assertEquals("User", actual.lastName());
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Update my profile - valid data returns updated profile")
    void updateMyProfile_ValidData_ShouldReturnUpdatedProfile() throws Exception {
        // Given
        UserUpdateProfileRequestDto requestDto = new UserUpdateProfileRequestDto(
                "admin@example.com", null, "UpdatedFirst", "UpdatedLast");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        put("/users/me")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        Assertions.assertEquals("admin@example.com", actual.email());
        Assertions.assertEquals("UpdatedFirst", actual.firstName());
        Assertions.assertEquals("UpdatedLast", actual.lastName());
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Update user role - manager assigns manager role to customer")
    void updateUserRole_ManagerUpdatesCustomer_ShouldReturnUpdatedUser() throws Exception {
        // Given
        Long customerId = 2L;
        RoleRequestDto roleRequestDto = new RoleRequestDto("ROLE_MANAGER");
        String jsonRequest = objectMapper.writeValueAsString(roleRequestDto);

        // When
        MvcResult result = mockMvc.perform(
                        put("/users/{id}/role", customerId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        Assertions.assertEquals(customerId, actual.id());
        Assertions.assertEquals("customer@example.com", actual.email());
    }

    @WithUserDetails("customer@example.com")
    @Test
    @DisplayName("Update user role - customer forbidden from updating role")
    void updateUserRole_CustomerRole_ShouldReturnForbidden() throws Exception {
        // Given
        Long targetUserId = 1L;
        RoleRequestDto roleRequestDto = new RoleRequestDto("ROLE_MANAGER");
        String jsonRequest = objectMapper.writeValueAsString(roleRequestDto);

        // When
        mockMvc.perform(
                        put("/users/{id}/role", targetUserId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails("admin@example.com")
    @Test
    @DisplayName("Update user role - user not found returns 404")
    void updateUserRole_UserNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        Long nonExistentUserId = 999L;
        RoleRequestDto roleRequestDto = new RoleRequestDto("ROLE_MANAGER");
        String jsonRequest = objectMapper.writeValueAsString(roleRequestDto);

        // When
        mockMvc.perform(
                        put("/users/{id}/role", nonExistentUserId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
