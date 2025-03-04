package com.project.LibManager.service.mapper;

import org.mapstruct.Mapper;

import com.project.LibManager.dto.response.RoleResponse;
import com.project.LibManager.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toRoleResponse(Role role);
}