package com.example.carsharingservice.dto.user;

import jakarta.validation.constraints.Email;

public record UserUpdateProfileRequestDto(
        @Email
        String email,
        String password,
        String firstName,
        String lastName
){
}
