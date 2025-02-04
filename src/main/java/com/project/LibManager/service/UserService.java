package com.project.LibManager.service;

import com.project.LibManager.dto.request.RegisterRequest;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.Role;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.exception.ErrorCode;
import com.project.LibManager.repository.RoleRepository;
import com.project.LibManager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    public String createUser(RegisterRequest request) {
        User user = new User();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        Role role = roleRepository.findById(Long.valueOf(1)).get();
        user.setRoles((Set<Role>) role);
        user.setFullName(request.getFullName());
        user.setBirthDate(request.getBirthDate());
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        return "Success";
    }
}
