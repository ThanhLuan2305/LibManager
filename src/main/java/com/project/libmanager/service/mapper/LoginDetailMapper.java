package com.project.libmanager.service.mapper;

import com.project.libmanager.entity.LoginDetail;
import com.project.libmanager.service.dto.request.LoginDetailRequest;
import com.project.libmanager.service.dto.response.LoginDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoginDetailMapper {
    LoginDetailResponse toLoginDetailResponse(LoginDetail loginDetail);

    LoginDetail toLoginDetail (LoginDetailRequest loginDetailRequest);

    void updateLoginDetail(@MappingTarget LoginDetail loginDetail, LoginDetailRequest loginDetailRequest);
}
