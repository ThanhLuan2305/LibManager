package com.project.libmanager.validation;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.VerificationStatus;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import org.springframework.stereotype.Component;

@Component
public class UserStatusValidator {
    public boolean validate(User user) {
        // check status user
        if (user.isDeleted()) {
            throw new AppException(ErrorCode.USER_IS_DELETED);
        }
        // check status verify user
        if (user.getVerificationStatus() != VerificationStatus.FULLY_VERIFIED) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED);
        }
        return true;
    }
}
