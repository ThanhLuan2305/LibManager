package com.project.LibManager.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.project.LibManager.service.*.*(..))") 
    public void logBeforeMethod(JoinPoint joinPoint) {
        log.info("Starting method execution: " + joinPoint.getSignature().toShortString());
    }

    @After("execution(* com.project.LibManager.service.*.*(..))") 
    public void logAfterMethod(JoinPoint joinPoint) {
        log.info("Method execution completed: " + joinPoint.getSignature().toShortString());
    }

    @AfterReturning(value = "execution(* com.project.LibManager.service.*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Method {} returned: {}", joinPoint.getSignature().toShortString(), result);
    }

    @AfterThrowing(value = "execution(* com.project.LibManager.service.*.*(..))", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("Method {} encountered an error: {}", joinPoint.getSignature().toShortString(), exception.getMessage());
    }

}

