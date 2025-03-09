package com.project.libmanager.validation;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import org.springframework.stereotype.Component;

@Component
public class UserStatusValidator {
    public void validate(User user) {
        if (user.isDeleted()) {
            throw new AppException(ErrorCode.USER_IS_DELETED);
        }
        if (!user.isVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        if (user.isResetPassword()) {
            throw new AppException(ErrorCode.USER_NEED_CHANGE_PASSWORD);
        }
    }
}
