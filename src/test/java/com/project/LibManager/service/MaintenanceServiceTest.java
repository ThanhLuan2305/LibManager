//package com.project.LibManager.service;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//
//import com.project.LibManager.service.impl.MaintenanceServiceImpl;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@SpringBootTest
//@TestPropertySource("/test.properties")
//public class MaintenanceServiceTest {
//    @Mock
//    private MaintenanceServiceImpl maintenanceService;
//
//    @BeforeEach
//    void setUp() {
//        maintenanceService = new MaintenanceServiceImpl();
//    }
//
//    @Test
//    void setMaintenanceMode_ShouldEnableMaintenanceMode() {
//        // Act
//        maintenanceService.setMaintenanceMode(true);
//
//        // Assert
//        assertTrue(maintenanceService.isMaintenanceMode(), "Maintenance mode should be enabled.");
//    }
//
//    @Test
//    void setMaintenanceMode_ShouldDisableMaintenanceMode() {
//        // Act
//        maintenanceService.setMaintenanceMode(false);
//
//        // Assert
//        assertFalse(maintenanceService.isMaintenanceMode(), "Maintenance mode should be disabled.");
//    }
//}
