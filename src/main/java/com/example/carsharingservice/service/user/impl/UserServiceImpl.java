package com.example.carsharingservice.service.user.impl;

import com.example.carsharingservice.dto.role.RoleRequestDto;
import com.example.carsharingservice.dto.user.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.user.UserResponseDto;
import com.example.carsharingservice.dto.user.UserUpdateProfileRequestDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.RegistrationException;
import com.example.carsharingservice.mapper.UserMapper;
import com.example.carsharingservice.model.Role;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.role.RoleRepository;
import com.example.carsharingservice.repository.user.UserRepository;
import com.example.carsharingservice.service.role.RoleService;
import com.example.carsharingservice.service.user.UserService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegistrationException("Unable to complete registration.");
        }
        User user = userMapper.toModel(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = roleService.findByName(Role.RoleName.ROLE_CUSTOMER);
        user.getRoles().add(role);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getProfileInfo(User user) {
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateProfileInfo(User user,
                                             UserUpdateProfileRequestDto requestDto) {
        userMapper.updateToModel(user, requestDto);
        if (requestDto.password() != null) {
            user.setPassword(passwordEncoder.encode(requestDto.password()));
        }
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateUserRole(RoleRequestDto roleRequestDto, Long id) {
        String roleName = roleRequestDto.roleName();
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cant find user by id: " + id)
        );
        Role role = roleRepository.findByName(Role.RoleName.valueOf(roleName)).orElseThrow(
                () -> new EntityNotFoundException("Cant find role by name: " + roleName)
        );
        Set<Role> updateRoles = new HashSet<>(user.getRoles());
        updateRoles.add(role);
        user.setRoles(updateRoles);
        return userMapper.toDto(userRepository.save(user));
    }
}
