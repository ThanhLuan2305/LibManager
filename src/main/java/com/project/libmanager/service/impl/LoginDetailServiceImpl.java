package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.entity.LoginDetail;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.LoginDetailRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.ILoginDetailService;
import com.project.libmanager.service.dto.request.LoginDetailRequest;
import com.project.libmanager.service.dto.response.LoginDetailResponse;
import com.project.libmanager.service.mapper.LoginDetailMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class LoginDetailServiceImpl implements ILoginDetailService {
    private final LoginDetailRepository loginDetailRepository;
    private final LoginDetailMapper loginDetailMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createLoginDetail(LoginDetailRequest loginRequest) {
        if(loginDetailRepository.existsByJti(loginRequest.getJti())) {
            throw  new AppException(ErrorCode.JTI_TOKEN_EXISTED);
        }
        LoginDetail loginDetail = loginDetailMapper.toLoginDetail(loginRequest);

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        loginDetail.setUser(user);

        loginDetailRepository.save(loginDetail);
    }

    @Override
    @Transactional
    public void updateLoginDetail(LoginDetailRequest loginRequest, Long id) {
        LoginDetail loginDetail = loginDetailRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.LOGINDETAIL_NOTFOUND));

        loginDetailMapper.updateLoginDetail(loginDetail, loginRequest);

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        loginDetail.setUser(user);

        loginDetailRepository.save(loginDetail);
    }

    @Override
    @Transactional
    public void disableLoginDetailById(String jti) {
        LoginDetail loginDetail = loginDetailRepository.findByJti(jti).orElseThrow(() -> new AppException(ErrorCode.LOGINDETAIL_NOTFOUND));
        loginDetail.setEnabled(false);
        loginDetailRepository.save(loginDetail);
    }

    @Override
    @Transactional
    public void updateLoginDetailIsEnable(String jti, Instant expTime) {
        LoginDetail loginDetail = loginDetailRepository.findByJtiAndEnabled(jti).orElseThrow(() -> new AppException(ErrorCode.LOGINDETAIL_NOTFOUND));
        loginDetail.setExpiredAt(expTime);
        loginDetailRepository.save(loginDetail);

    }

    @Override
    public LoginDetailResponse getLoginDetailByJti(String jti) {
        return loginDetailMapper.toLoginDetailResponse(loginDetailRepository.findByJti(jti).orElseThrow(() -> new AppException(ErrorCode.LOGINDETAIL_NOTFOUND)));
    }
}
