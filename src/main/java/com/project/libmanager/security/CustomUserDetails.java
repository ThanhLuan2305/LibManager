package com.project.libmanager.security;

import com.project.libmanager.entity.User;
import com.project.libmanager.validation.UserStatusValidator;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    private final transient User user;
    private final Set<GrantedAuthority> authorities;
    private final transient UserStatusValidator userStatusValidator;

    public CustomUserDetails(User user, UserStatusValidator userStatusValidator) {
        this.user = user;
        this.userStatusValidator = userStatusValidator;
        this.authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) role::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return userStatusValidator.validate(user);
    }
}
