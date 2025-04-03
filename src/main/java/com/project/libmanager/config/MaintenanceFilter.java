package com.project.libmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.service.IMaintenanceService;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

    // Constructor: Sets up the filter with the maintenance service
    public MaintenanceFilter(IMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Convert request and response to HTTP types for web use
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get the URL of the request (e.g., "/auth/login")
        String requestURI = httpRequest.getRequestURI();

        // Check if the URL is allowed to skip the filter
        if (requestURI.equals("/auth/login") ||
                requestURI.equals("/config/maintenance/status") ||
                requestURI.equals("/auth/refresh") ||
                requestURI.equals("/auth/info")) {
            // Let the request continue without checking maintenance mode
            chain.doFilter(request, response);
            return;
        }

        // Check if the app is in maintenance mode
        if (maintenanceService.isMaintenanceMode()) {
            // Get the user's authentication info (if logged in)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Check if the user is an admin
            boolean isAdmin = authentication != null &&
                    authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            // If the user is an admin, let the request continue
            if (isAdmin) {
                chain.doFilter(request, response);
                return;
            }

            // If not an admin, send an error response
            // Set status to 503 (Service Unavailable)
            httpResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            // Set response type to JSON
            httpResponse.setContentType("application/json");
            // Set character encoding to UTF-8
            httpResponse.setCharacterEncoding("UTF-8");

            // Create error details from ErrorCode
            ErrorCode errorCode = ErrorCode.MAINTENACE_MODE;
            Map<String, Object> errorResponse = Map.of(
                    "code", errorCode.getCode(),
                    "message", errorCode.getMessage());

            // Convert error details to JSON format
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);

            // Send the JSON error to the user
            httpResponse.getWriter().write(jsonResponse);
            return;
        }

        // If maintenance mode is off, let the request continue
        chain.doFilter(request, response);
    }
}