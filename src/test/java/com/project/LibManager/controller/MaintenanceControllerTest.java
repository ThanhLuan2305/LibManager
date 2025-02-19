package com.project.LibManager.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.LibManager.service.IMaintenanceService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class MaintenanceControllerTest {
     @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IMaintenanceService maintenanceService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void setMaintenanceMode_success() throws Exception {
        // GIVEN
        boolean status = true;

        Mockito.doNothing().when(maintenanceService).setMaintenanceMode(ArgumentMatchers.anyBoolean());

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/config/admin/maintenance/" + status)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Server in Maintenance: " + status));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setMaintenanceMode_fail() throws Exception {
        // GIVEN
        boolean status = true;

        // Giả lập service ném ra ngoại lệ
        Mockito.doThrow(new RuntimeException("Unexpected error occurred"))
            .when(maintenanceService).setMaintenanceMode(ArgumentMatchers.anyBoolean());

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/config/admin/maintenance/" + status)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Unexpected error occurred"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMaintenanceMode_success() throws Exception {
        // GIVEN
        boolean isMaintenance = true;
        Mockito.when(maintenanceService.isMaintenanceMode()).thenReturn(isMaintenance);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/config/admin/maintenance/status")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("The system is currently under maintenance."))
                .andExpect(MockMvcResultMatchers.jsonPath("result.maintenanceMode")
                        .value(isMaintenance));
    }
}
