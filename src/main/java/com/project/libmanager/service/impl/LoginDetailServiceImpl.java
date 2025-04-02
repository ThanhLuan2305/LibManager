package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.entity.LoginDetail;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.LoginDetailRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.ILoginDetailService;
import com.project.libmanager.service.dto.request.LoginDetailRequest;
import com.project.libmanager.service.dto.response.LoginDetailResponse;
import com.project.libmanager.service.mapper.LoginDetailMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Implementation of {@link ILoginDetailService} for managing login detail operations.
 * Handles creation, updates, and deletion of login details associated with user sessions.
 */
@Service
@AllArgsConstructor
@Slf4j
public class LoginDetailServiceImpl implements ILoginDetailService {
    private final LoginDetailRepository loginDetailRepository; // Repository for login detail persistence
    private final LoginDetailMapper loginDetailMapper;         // Mapper for DTO-entity conversions
    private final UserRepository userRepository;               // Repository for user data access

    /**
     * Creates a new login detail entry for a user session.
     *
     * @param loginRequest the {@link LoginDetailRequest} containing:
     *                     - jti: unique JWT identifier (required)
     *                     - email: user's email (required)
     *                     - other login metadata (e.g., issuedAt, expiredAt)
     * @throws AppException if:
     *                      - JTI already exists (ErrorCode.JTI_TOKEN_EXISTED)
     *                      - user not found by email (ErrorCode.ROLE_NOT_EXISTED)
     * @implNote Ensures JTI uniqueness, links login detail to user, and saves transactionally.
     */
    @Override
    @Transactional
    public void createLoginDetail(LoginDetailRequest loginRequest) {
        // Check JTI uniqueness to prevent duplicate session tokens
        if (loginDetailRepository.existsByJti(loginRequest.getJti())) {
            throw new AppException(ErrorCode.JTI_TOKEN_EXISTED); // Fail fast if JTI is taken
        }
        // Map request DTO to entity; assumes all required fields are present
        LoginDetail loginDetail = loginDetailMapper.toLoginDetail(loginRequest);

        // Fetch user by email; assumes email is unique, error code ROLE_NOT_EXISTED seems incorrect
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        // Associate login detail with user; establishes relationship
        loginDetail.setUser(user);

        // Persist login detail; transactional ensures atomicity
        loginDetailRepository.save(loginDetail);
    }

    /**
     * Updates an existing login detail entry.
     *
     * @param loginRequest the {@link LoginDetailRequest} containing updated fields:
     *                     - jti: JWT identifier (not updated)
     *                     - email: user's email (required)
     *                     - other metadata (e.g., expiredAt)
     * @param id           the unique identifier of the login detail to update
     * @throws AppException if:
     *                      - login detail not found (ErrorCode.LOGINDETAIL_NOTFOUND)
     *                      - user not found by email (ErrorCode.ROLE_NOT_EXISTED)
     * @implNote Updates login detail fields and user association transactionally.
     */
    @Override
    @Transactional
    public void updateLoginDetail(LoginDetailRequest loginRequest, Long id) {
        // Fetch existing login detail by ID; fails if not found
        LoginDetail loginDetail = loginDetailRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOGINDETAIL_NOTFOUND));

        // Apply updates from request; assumes mapper merges fields correctly
        loginDetailMapper.updateLoginDetail(loginDetail, loginRequest);

        // Fetch user by email; re-links user even if unchanged, error code seems off
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        // Update user association; ensures consistency
        loginDetail.setUser(user);

        // Save updated entity; transactional ensures rollback on failure
        loginDetailRepository.save(loginDetail);
    }

    /**
     * Disables a login detail by setting its enabled status to false.
     *
     * @param jti the JWT identifier of the login detail to disable
     * @throws AppException if login detail not found (ErrorCode.LOGINDETAIL_NOTFOUND)
     * @implNote Marks login detail as disabled transactionally to invalidate session.
     */
    @Override
    @Transactional
    public void disableLoginDetailById(String jti) {
        // Fetch login detail by JTI; assumes JTI is unique
        LoginDetail loginDetail = loginDetailRepository.findByJti(jti)
                .orElseThrow(() -> new AppException(ErrorCode.LOGINDETAIL_NOTFOUND));
        // Disable session; prevents further use of token
        loginDetail.setEnabled(false);
        // Persist change; transactional ensures atomicity
        loginDetailRepository.save(loginDetail);
    }

    /**
     * Updates the expiration time of an enabled login detail.
     *
     * @param jti     the JWT identifier of the login detail
     * @param expTime the new expiration time to set
     * @throws AppException if enabled login detail not found (ErrorCode.LOGINDETAIL_NOTFOUND)
     * @implNote Updates expiration time transactionally; only applies to enabled sessions.
     */
    @Override
    @Transactional
    public void updateLoginDetailIsEnable(String jti, Instant expTime) {
        // Fetch enabled login detail by JTI; ensures only active sessions are updated
        LoginDetail loginDetail = loginDetailRepository.findByJtiAndEnabled(jti)
                .orElseThrow(() -> new AppException(ErrorCode.LOGINDETAIL_NOTFOUND));
        // Update expiration time; assumes expTime is valid
        loginDetail.setExpiredAt(expTime);
        // Save changes; transactional ensures consistency
        loginDetailRepository.save(loginDetail);
    }

    /**
     * Retrieves a login detail by its JTI.
     *
     * @param jti the JWT identifier of the login detail
     * @return a {@link LoginDetailResponse} containing login detail data
     * @throws AppException if login detail not found (ErrorCode.LOGINDETAIL_NOTFOUND)
     * @implNote Maps entity to response DTO; assumes mapper handles all fields.
     */
    @Override
    public LoginDetailResponse getLoginDetailByJti(String jti) {
        // Fetch and map login detail in one step; concise but relies on repository
        return loginDetailMapper.toLoginDetailResponse(
                loginDetailRepository.findByJti(jti)
                        .orElseThrow(() -> new AppException(ErrorCode.LOGINDETAIL_NOTFOUND)));
    }

    /**
     * Deletes all login details associated with a user.
     *
     * @param user the {@link User} whose login details should be deleted
     * @throws AppException if deletion fails (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Performs bulk deletion transactionally; logs errors for debugging.
     */
    @Override
    @Transactional
    public void deleteLoginDetailByUser(User user) {
        try {
            // Delete all login details for user; assumes efficient repository query
            loginDetailRepository.deleteByUser(user);
        } catch (Exception e) {
            log.error("Error when deleting login detail: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}