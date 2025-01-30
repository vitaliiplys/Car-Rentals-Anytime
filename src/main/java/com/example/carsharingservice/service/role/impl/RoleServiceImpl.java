package com.example.carsharingservice.service.role.impl;

import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.model.Role;
import com.example.carsharingservice.repository.role.RoleRepository;
import com.example.carsharingservice.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role findByName(Role.RoleName role) {
        return roleRepository.findByName(role).orElseThrow(
                () -> new EntityNotFoundException("Can`t find role " + role));
    }
}
