package com.project.libmanager.security;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IUserService;
import com.project.libmanager.validation.UserStatusValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService} for loading user details.
 * Retrieves user information by email and constructs a {@link UserDetails} object for authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final IUserService userService;                // Service for user data retrieval
    private final UserStatusValidator userStatusValidator; // Validator for user status checks

    /**
     * Loads user details by username (email) for Spring Security authentication.
     *
     * @param username the email address of the user to load (used as username)
     * @return a {@link UserDetails} object containing user information and authorities
     * @throws UsernameNotFoundException if the user is not found (wrapped as {@link AppException})
     * @throws AppException              if the user does not exist (ErrorCode.USER_NOT_EXISTED)
     * @implNote Fetches user by email via {@link IUserService} and wraps in {@link CustomUserDetails}.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user by email; assumes email is the username in this system
        User user = userService.findByEmail(username);
        // Check if user exists; null check to enforce existence
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        // Construct UserDetails; includes user data and status validation
        return new CustomUserDetails(user, userStatusValidator);
    }
}