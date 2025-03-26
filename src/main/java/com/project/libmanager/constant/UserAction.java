package com.project.libmanager.constant;

public enum UserAction {
    // Xác thực & phiên làm việc
    LOGIN,
    LOGOUT,
    PASSWORD_CHANGED,
    PASSWORD_RESET_REQUEST,
    PASSWORD_RESET_SUCCESS,

    // Quản lý tài khoản
    REGISTER,
    EMAIL_VERIFICATION,
    PHONE_VERIFICATION,
    CHANGED_EMAIL,
    CHANGED_PHONE,

    // Hành động quản trị viên
    ADMIN_CREATE_USER,
    ADMIN_DELETE_USER,
    ADMIN_UPDATE_USER,

    // Quản lý dữ liệu & bảo mật
    SYSTEM_MAINTENANCE_MODE,

    // Tương tác với hệ thống
    BOOK_BORROWED,
    BOOK_RETURNED,
    ADD_BOOK,
    DELETE_BOOK,
    UPDATE_BOOK_INFO
}

