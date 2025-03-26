package com.project.libmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.service.IMaintenanceService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class MaintenanceFilter implements Filter {
    private final IMaintenanceService maintenanceService;

    public MaintenanceFilter(IMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.equals("/auth/login") ||
                requestURI.equals("/config/getMaintenanceMode") ||
                requestURI.equals("/auth/refresh") ||
                requestURI.equals("/auth/info")) {
            chain.doFilter(request, response);
            return;
        }

        if (maintenanceService.isMaintenanceMode()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            boolean isAdmin = authentication != null &&
                    authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            if (isAdmin) {
                chain.doFilter(request, response);
                return;
            }

            httpResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");

            ErrorCode errorCode = ErrorCode.MAINTENACE_MODE;
            Map<String, Object> errorResponse = Map.of(
                    "code", errorCode.getCode(),
                    "message", errorCode.getMessage());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);

            httpResponse.getWriter().write(jsonResponse);
            return;
        }

        chain.doFilter(request, response);
    }

}
