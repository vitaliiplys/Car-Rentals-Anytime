package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.role.RoleRequestDto;
import com.example.carsharingservice.dto.user.UserResponseDto;
import com.example.carsharingservice.dto.user.UserUpdateProfileRequestDto;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users controller", description = "Endpoints for managing users")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get user`s profile information",
            description = "Endpoint for getting user`s profile information")
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getMyProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getProfileInfo(user);
    }

    @Operation(summary = "Update user`s profile information",
            description = "Endpoint for updating user`s profile information")
    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto updateMyProfile(
            Authentication authentication,
            @RequestBody UserUpdateProfileRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return userService.updateProfileInfo(user, requestDto);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Update user role", description = "Update user role by user id")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto updateUserRole(
            @RequestBody RoleRequestDto roleRequestDto, @PathVariable Long id) {
        return userService.updateUserRole(roleRequestDto, id);
    }
}
