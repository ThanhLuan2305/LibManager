package com.project.LibManager.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.LibManager.service.IMaintenanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements IMaintenanceService {
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
    }
}
