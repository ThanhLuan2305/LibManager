package com.project.libmanager.service.impl;

import com.project.libmanager.entity.User;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.util.AsyncMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.libmanager.service.IMaintenanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements IMaintenanceService {
    private final UserRepository userRepository;
    private final AsyncMailSender asyncMailSender;
    @Value("${app.maintenance-mode:false}")
    private boolean maintenanceMode;

    /**
     * Returns the current status of the maintenance mode.
     * 
     * @return {@code true} if the application is in maintenance mode, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    /**
     * Sets the maintenance mode status.
     * 
     * @param maintenanceMode The desired maintenance mode status.
     *                        Pass {@code true} to enable maintenance mode,
     *                        {@code false} to disable it.
     */
    @Override
    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;

        List<String> emails = userRepository.findAll().stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .toList();

        asyncMailSender.sendMaintenanceEmails(emails, maintenanceMode);
    }
}
