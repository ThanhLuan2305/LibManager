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

/**
 * Custom implementation of Spring Security's {@link UserDetails} for representing user details.
 * Encapsulates user information, roles as authorities, and status validation.
 */
@Getter
public class CustomUserDetails implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L; // Serialization version ID

    private final transient User user;                         // User entity, transient to avoid serialization
    private final Set<GrantedAuthority> authorities;           // Set of user roles as authorities
    private final transient UserStatusValidator userStatusValidator; // Validator for user status

    /**
     * Constructs a CustomUserDetails instance from a User entity and status validator.
     *
     * @param user                the {@link User} entity containing user data
     * @param userStatusValidator the {@link UserStatusValidator} to check user status
     * @implNote Maps user roles to authorities using role names; assumes role name is authority.
     */
    public CustomUserDetails(User user, UserStatusValidator userStatusValidator) {
        this.user = user;
        this.userStatusValidator = userStatusValidator;
        // Map roles to authorities; converts role names to GrantedAuthority
        this.authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) role::getName) // Use role name as authority
                .collect(Collectors.toSet());                  // Collect as unmodifiable set
    }

    /**
     * Returns the authorities granted to the user.
     *
     * @return a {@link Collection} of {@link GrantedAuthority} representing user roles
     * @implNote Returns the precomputed set of authorities from constructor.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return authorities; immutable set from constructor
        return authorities;
    }

    /**
     * Returns the user's password.
     *
     * @return the encoded password from the {@link User} entity
     * @implNote Delegates to user entity's password field.
     */
    @Override
    public String getPassword() {
        // Return user's password; assumes encoded in User entity
        return user.getPassword();
    }

    /**
     * Returns the username used for authentication.
     *
     * @return the user's email as the username
     * @implNote Uses email as the unique identifier for authentication.
     */
    @Override
    public String getUsername() {
        // Return user's email; serves as username in this system
        return user.getEmail();
    }

    /**
     * Indicates whether the user's account is enabled.
     *
     * @return true if the user is enabled (passes validation), false otherwise
     * @implNote Delegates to {@link UserStatusValidator} to check user status (e.g., not deleted/banned).
     */
    @Override
    public boolean isEnabled() {
        // Check user status; relies on validator for custom logic
        return userStatusValidator.validate(user);
    }
}