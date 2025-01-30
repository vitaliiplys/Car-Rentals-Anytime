package com.example.carsharingservice.service.user;

import com.example.carsharingservice.dto.role.RoleRequestDto;
import com.example.carsharingservice.dto.user.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.user.UserResponseDto;
import com.example.carsharingservice.dto.user.UserUpdateProfileRequestDto;
import com.example.carsharingservice.exception.RegistrationException;
import com.example.carsharingservice.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto request) throws RegistrationException;

    UserResponseDto getProfileInfo(User user);

    UserResponseDto updateProfileInfo(User user, UserUpdateProfileRequestDto requestDto);

    UserResponseDto updateUserRole(RoleRequestDto roleRequestDto, Long id);
}
