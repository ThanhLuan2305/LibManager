package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.criteria.UserCriteria;
import com.project.libmanager.entity.LoginDetail;
import com.project.libmanager.entity.Role;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.BorrowingRepository;
import com.project.libmanager.repository.LoginDetailRepository;
import com.project.libmanager.repository.RoleRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.ILoginDetailService;
import com.project.libmanager.service.IUserService;
import com.project.libmanager.service.dto.request.UserCreateRequest;
import com.project.libmanager.service.dto.request.UserUpdateRequest;
import com.project.libmanager.service.dto.response.UserResponse;
import com.project.libmanager.service.mapper.UserMapper;
import com.project.libmanager.specification.UserQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IUserService} for managing user-related operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;              // Repository for user CRUD operations
    private final RoleRepository roleRepository;              // Repository for role lookups
    private final UserMapper userMapper;                      // Mapper for entity-DTO conversion
    private final PasswordEncoder passwordEncoder;            // Utility for password encryption
    private final UserQueryService userQueryService;          // Service for complex user queries
    private final IActivityLogService activityLogService;     // Service for logging actions
    private final BorrowingRepository borrowingRepository;    // Repository for borrowing records
    private final LoginDetailRepository loginDetailRepository; // Repository for login details
    private final ILoginDetailService loginDetailService;     // Service for managing login details

    /**
     * Creates a new user and assigns a default role.
     *
     * @param request the request object containing user creation details:
     *                - email: unique email address of the new user
     *                - password: raw password to be encrypted
     *                - listRole: set of role names to assign
     * @return a {@link UserResponse} containing the created user's details:
     * - id: unique identifier of the user
     * - email: user's email address
     * - roles: set of assigned role names
     * @throws AppException if:
     *                      - email already exists (ErrorCode.USER_EXISTED)
     *                      - any role does not exist (ErrorCode.ROLE_NOT_EXISTED)
     *                      - database error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     *                      - admin is not authenticated (ErrorCode.UNAUTHORIZED)
     * @implNote This method encrypts the user's password and assigns the default
     * user role. The user is marked as not verified.
     */
    @Transactional
    @Override
    public UserResponse createUser(UserCreateRequest request) {
        // Convert request DTO to User entity
        User user = userMapper.toUser(request);
        // Initialize set for storing roles
        Set<Role> roles = new HashSet<>();

        // Encrypt the raw password from request and set it to the user entity
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Check if the email already exists in the database
        if (userRepository.existsByEmail(request.getEmail())) {
            // Throw exception if email is already taken
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Populate roles set with Role entities based on role names from request
        roles.addAll(request.getListRole().stream()
                .map(x -> roleRepository.findByName(x) // Fetch each role by name
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED))) // Throw if role not found
                .collect(Collectors.toSet())); // Collect into set

        // Get the authenticated admin performing this action
        User userAction = getAuthenticatedUser();
        try {
            // Assign roles to the new user
            user.setRoles(roles);
            // Save the new user to the database
            userRepository.save(user);
            // Convert saved user to response DTO
            UserResponse userResponse = userMapper.toUserResponse(user);
            // Log the creation action by admin
            activityLogService.logAction(
                    userAction.getId(),
                    userAction.getEmail(),
                    UserAction.ADMIN_CREATE_USER,
                    "Admin create new user with email: " + user.getEmail(),
                    null,
                    userResponse
            );

            // Return the response to the caller
            return userResponse;
        } catch (DataIntegrityViolationException exception) {
            // Handle database-specific errors (e.g., constraint violations)
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return a {@link User} entity representing the authenticated user, typically an admin
     * @throws AppException if:
     *                      - no security context or authentication exists (ErrorCode.UNAUTHORIZED)
     *                      - authenticated user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote This method extracts the email from the security context and queries
     * the database to return the corresponding user.
     */
    private User getAuthenticatedUser() {
        // Get current security context holding authentication details
        SecurityContext jwtContext = SecurityContextHolder.getContext();
        // Check if context or authentication is invalid or user is not authenticated
        if (jwtContext == null || jwtContext.getAuthentication() == null ||
                !jwtContext.getAuthentication().isAuthenticated()) {
            // Throw exception if authentication is missing or invalid
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // Log the authenticated user's email for debugging
        log.info("Authentication {}", jwtContext.getAuthentication().getName());

        // Extract email from authentication object
        String email = jwtContext.getAuthentication().getName();
        // Fetch user by email from database
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)); // Throw if not found
    }

    /**
     * Fetches a paginated list of all users.
     *
     * @param pageable the pagination details:
     *                 - page: page number
     *                 - size: number of items per page
     *                 - sort: sorting criteria
     * @return a {@link Page} of {@link UserResponse} containing user details
     * @throws AppException if no users exist (ErrorCode.USER_NOT_EXISTED)
     * @implNote This method fetches all users from the repository and returns them
     * in a paginated format.
     */
    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        // Fetch all users with pagination from repository
        Page<User> pageUser = userRepository.findAll(pageable);
        // Check if the page is empty (no users found)
        if (pageUser.isEmpty()) {
            // Throw exception if no users exist
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        // Convert page of users to page of responses
        return mapUserPageUserResponsePage(pageUser);
    }

    /**
     * Converts a page of users to a page of user responses.
     *
     * @param userPage the {@link Page} of {@link User} entities to convert
     * @return a {@link Page} of {@link UserResponse} containing mapped user details
     * @implNote This method maps the content of the user page to a list of user
     * responses and returns the paginated response.
     */
    @Override
    public Page<UserResponse> mapUserPageUserResponsePage(Page<User> userPage) {
        // Map each user entity to a response DTO
        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(user -> mapToUserResponseByMapper(user.getId())) // Convert user by ID
                .toList(); // Collect into list

        // Create and return a new paginated response with mapped data
        return new PageImpl<>(userResponses, userPage.getPageable(), userPage.getTotalElements());
    }

    /**
     * Fetches the response of a user by their ID.
     *
     * @param id the unique identifier of the user to map
     * @return a {@link UserResponse} containing the user's details
     * @throws AppException if the user does not exist (ErrorCode.USER_NOT_EXISTED)
     * @implNote This method retrieves a specific user by ID and returns the user
     * response.
     */
    @Override
    public UserResponse mapToUserResponseByMapper(Long id) {
        // Fetch user by ID from repository
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Convert user entity to response DTO
        return userMapper.toUserResponse(user);
    }

    /**
     * Fetches the details of a specific user.
     *
     * @param id the unique identifier of the user to retrieve
     * @return a {@link UserResponse} containing the user's details
     * @throws AppException if the user does not exist (ErrorCode.USER_NOT_EXISTED)
     * @implNote This method retrieves a specific user and returns their response.
     */
    @Override
    public UserResponse getUser(Long id) {
        // Fetch user by ID and map directly to response DTO
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    /**
     * Fetches the details of the currently authenticated user.
     *
     * @return a {@link UserResponse} containing the authenticated user's details
     * @throws AppException if:
     *                      - user is not authenticated (ErrorCode.UNAUTHORIZED)
     *                      - user does not exist (ErrorCode.USER_NOT_EXISTED)
     *                      - unexpected error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote This method retrieves the currently logged-in user using the
     * security context.
     */
    @Override
    public UserResponse getMyInfo() {
        try {
            // Get current security context
            SecurityContext jwtContext = SecurityContextHolder.getContext();

            // Validate security context and authentication
            if (jwtContext == null || jwtContext.getAuthentication() == null ||
                    !jwtContext.getAuthentication().isAuthenticated()) {
                // Throw exception if authentication is missing or invalid
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            // Extract email from authentication object
            String email = jwtContext.getAuthentication().getName();

            // Fetch user by email from database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_IS_PRIVATE));

            // check null user (already handled by orElseThrow)
            if (user == null) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }

            // Convert user entity to response DTO
            return userMapper.toUserResponse(user);
        } catch (AppException e) {
            log.error("Error getting user info: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in getMyInfo: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Updates the information of an existing user.
     *
     * @param id      the unique identifier of the user to update
     * @param request the request object containing updated user details:
     *                - email: new email address (optional)
     *                - password: new password (optional)
     *                - listRole: new set of role names (optional)
     * @return a {@link UserResponse} containing the updated user's details
     * @throws AppException if:
     *                      - user does not exist (ErrorCode.USER_NOT_EXISTED)
     *                      - user is an admin (ErrorCode.CANNOT_UPDATE_ADMIN)
     *                      - any role does not exist (ErrorCode.ROLE_NOT_EXISTED)
     *                      - unexpected error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote This method updates the user's information, password.
     */
    @Transactional
    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        // Fetch existing user by ID
        User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Store current user state as response DTO to save activity log
        UserResponse oldeUserResponse = userMapper.toUserResponse(u);
        // Check if user has ADMIN role
        if (u.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_ADMIN);
        }
        // Get authenticated admin performing the update
        User userAction = getAuthenticatedUser();
        try {
            // Store current encrypted password
            String oldPassword = u.getPassword();
            // Update user fields from request DTO
            userMapper.updateUser(u, request);

            // Check if new password is provided and not blank
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                // Encrypt and set new password if provided
                u.setPassword(passwordEncoder.encode(request.getPassword()));
            } else {
                // Retain old password if no new one provided
                u.setPassword(oldPassword);
            }

            // Initialize and populate new roles set from request
            Set<Role> roles = new HashSet<>(request.getListRole().stream()
                    .map(x -> roleRepository.findByName(x) // Fetch each role by name
                            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)))
                    .collect(Collectors.toSet()));

            // Assign updated roles to user
            u.setRoles(roles);

            // Save updated user to database
            User newUser = userRepository.save(u);
            // Convert updated user to response DTO
            UserResponse userResponse = userMapper.toUserResponse(newUser);
            // Log the update action by admin
            activityLogService.logAction(
                    userAction.getId(),
                    userAction.getEmail(),
                    UserAction.ADMIN_UPDATE_USER,
                    "Admin update user with email: " + u.getEmail(),
                    oldeUserResponse,
                    userResponse
            );
            // Return updated response
            return userResponse;
        } catch (AppException e) {
            log.error("Error updating user: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Deletes a user from the system.
     *
     * @param userId the unique identifier of the user to delete
     * @throws AppException if:
     *                      - user does not exist (ErrorCode.USER_NOT_EXISTED)
     *                      - user is an admin (ErrorCode.CANNOT_DELETE_ADMIN)
     *                      - user has active borrowings (ErrorCode.USER_CANNOT_BE_DELETED)
     *                      - unexpected error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote This method checks if the user has borrowings before deleting. If
     * so, the user is marked as deleted instead of being fully deleted.
     */
    @Transactional
    @Override
    public void deleteUser(Long userId) {
        // Fetch user by ID
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Check if user has ADMIN role
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ADMIN);
        }
        // Check if user has active borrowings (return date is null)
        if (borrowingRepository.existsByUserIdAndReturnDateIsNull(userId)) {
            throw new AppException(ErrorCode.USER_CANNOT_BE_DELETED);
        }

        // Get authenticated admin performing the deletion
        User userAction = getAuthenticatedUser();
        try {
            // Mark user as deleted (soft delete)
            user.setDeleted(true);
            // Fetch all login details associated with the user
            List<LoginDetail> userLoginDetails = loginDetailRepository.findByUserId(user.getId());
            // Iterate through login details and disable each one
            for (LoginDetail loginDetail : userLoginDetails) {
                // Disable login detail by its JTI (JWT identifier)
                loginDetailService.disableLoginDetailById(loginDetail.getJti());
            }
            // Save the updated (deleted) user
            userRepository.save(user);
            // Log the deletion action by admin
            activityLogService.logAction(
                    userAction.getId(),
                    userAction.getEmail(),
                    UserAction.ADMIN_DELETE_USER,
                    "Admin create new user with email: " + user.getEmail(),
                    userMapper.toUserResponse(userAction),
                    null
            );
        } catch (Exception e) {
            // Log and wrap unexpected exceptions in custom exception
            log.error("Error deleting user: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Searches for users based on specified criteria.
     *
     * @param criteria the {@link UserCriteria} object containing search parameters
     * @param pageable the pagination details:
     *                 - page: page number
     *                 - size: number of items per page
     *                 - sort: sorting criteria
     * @return a {@link Page} of {@link UserResponse} containing matching users
     * @implNote This method delegates to UserQueryService for criteria-based search
     * and maps results to a paginated response.
     */
    @Override
    public Page<UserResponse> searchUser(UserCriteria criteria, Pageable pageable) {
        // Fetch users based on search criteria with pagination
        Page<User> users = userQueryService.findByCriteria(criteria, pageable);
        // Convert page of users to page of responses
        return mapUserPageUserResponsePage(users);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user to find
     * @return a {@link User} entity matching the email
     * @throws AppException if no user exists with the given email (ErrorCode.USER_NOT_EXISTED)
     * @implNote This method queries the database by email and returns the user entity.
     */
    @Override
    public User findByEmail(String email) {
        // Fetch user by email from repository
        return userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}