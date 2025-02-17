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

    @Override
    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    @Override
    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }
}
