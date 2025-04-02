package com.project.libmanager.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for managing HTTP cookies in a servlet-based application.
 * Provides methods to add and remove cookies with secure configurations.
 */
@Component
public class CookieUtil {

    private static final Logger log = LoggerFactory.getLogger(CookieUtil.class); // Logger for debugging and tracking

    /**
     * Adds a cookie to the HTTP response with specified name, value, and expiration.
     *
     * @param response the {@link HttpServletResponse} to which the cookie will be added
     * @param name     the name of the cookie (e.g., "accessToken")
     * @param value    the value of the cookie (e.g., a JWT token)
     * @param maxAge   the maximum age of the cookie in seconds; determines expiration
     * @implNote Sets the cookie as HttpOnly and Secure, with a root path ("/").
     */
    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        // Log cookie details; aids in debugging (note: "Type token" might be a misnomer for "name")
        log.info("Type token: {}, time: {}", name, maxAge);
        // Create new cookie; name-value pair
        Cookie cookie = new Cookie(name, value);
        // Set HttpOnly; prevents client-side script access for security
        cookie.setHttpOnly(true);
        // Set Secure; ensures cookie is only sent over HTTPS
        cookie.setSecure(true);
        // Set root path; makes cookie accessible across entire domain
        cookie.setPath("/");
        // Set expiration; maxAge in seconds, after which cookie expires
        cookie.setMaxAge(maxAge);
        // Add cookie to response; commits it to client
        response.addCookie(cookie);
    }

    /**
     * Removes a cookie by setting its expiration to 0.
     *
     * @param response the {@link HttpServletResponse} to which the cookie removal will be applied
     * @param name     the name of the cookie to remove
     * @implNote Creates a cookie with an empty value and zero maxAge to effectively delete it.
     */
    public void removeCookie(HttpServletResponse response, String name) {
        // Create cookie with empty value; prepares for removal
        Cookie cookie = new Cookie(name, "");
        // Set root path; ensures removal applies to same scope as original cookie
        cookie.setPath("/");
        // Set HttpOnly; maintains security during removal
        cookie.setHttpOnly(true);
        // Set Secure; ensures removal occurs over HTTPS
        cookie.setSecure(true);
        // Set maxAge to 0; immediately expires the cookie, effectively deleting it
        cookie.setMaxAge(0);
        // Add cookie to response; commits removal to client
        response.addCookie(cookie);
    }
}