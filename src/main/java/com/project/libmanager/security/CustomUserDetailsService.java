package com.project.libmanager.security;

import com.project.libmanager.entity.User;
import com.project.libmanager.service.IUserService;
import com.project.libmanager.validation.UserStatusValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final IUserService userService;
    private final UserStatusValidator userStatusValidator;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByEmail(username);
        return new CustomUserDetails(user, userStatusValidator);
    }
}
