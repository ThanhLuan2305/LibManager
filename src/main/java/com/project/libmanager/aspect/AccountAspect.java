package com.project.libmanager.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AccountAspect {
    @Before("execution(* com.project.libmanager.service.impl.*.*(..))")
    public void logUserAction(JoinPoint joinPoint) {
        // Get info user from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "UNKNOWN";

        // Get IP Address from HttpServletRequest
        String ipAddress = getClientIp();

        // Get method is calling
        String methodName = joinPoint.getSignature().toShortString();

        // Log info
        log.info("User '{}' (IP: {}) is executing method: {}", username, ipAddress, methodName);
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // Get Ip if have proxy
                String ip = request.getHeader("X-Forwarded-For"); 
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("Could not get client IP: {}", e.getMessage());
        }
        return "UNKNOWN";
    }
}
