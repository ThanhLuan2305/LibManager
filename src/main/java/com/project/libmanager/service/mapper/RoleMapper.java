package com.project.libmanager.service.mapper;

import org.mapstruct.Mapper;

import com.project.libmanager.service.dto.response.RoleResponse;
import com.project.libmanager.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toRoleResponse(Role role);
}