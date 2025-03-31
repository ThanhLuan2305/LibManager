package com.project.libmanager.constant;

public enum UserAction {
    // Authentication
    LOGIN,
    LOGOUT,
    PASSWORD_CHANGED,
    PASSWORD_RESET_REQUEST,
    PASSWORD_RESET_SUCCESS,

    // Manage Account
    REGISTER,
    EMAIL_VERIFICATION,
    PHONE_VERIFICATION,
    CHANGED_EMAIL,
    CHANGED_PHONE,

    // Admin Action
    ADMIN_CREATE_USER,
    ADMIN_DELETE_USER,
    ADMIN_UPDATE_USER,

    // Admin configuration
    SYSTEM_MAINTENANCE_MODE,

    // Management
    BOOK_BORROWED,
    BOOK_RETURNED,
    ADD_BOOK,
    DELETE_BOOK,
    UPDATE_BOOK_INFO,
    IMPORT_BOOK_BY_CSV
}

