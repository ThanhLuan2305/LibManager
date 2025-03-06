package com.project.libmanager.service;

import com.nimbusds.jose.JOSEException;
import com.project.libmanager.service.dto.request.ChangeMailRequest;
import com.project.libmanager.service.dto.request.RegisterRequest;
import com.project.libmanager.service.dto.request.VerifyChangeMailRequest;
import com.project.libmanager.service.dto.response.UserResponse;

import java.text.ParseException;

public interface IAccountService {
    UserResponse registerUser(RegisterRequest registerRequest);
    boolean verifyEmail(String token) throws JOSEException, ParseException;
    void verifyChangeEmail(VerifyChangeMailRequest request);
    void changeEmail(ChangeMailRequest request);
}
