package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.criteria.UserCriteria;
import com.project.libmanager.entity.LoginDetail;
import com.project.libmanager.repository.BorrowingRepository;
import com.project.libmanager.repository.LoginDetailRepository;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.IAuthenticationService;
import com.project.libmanager.service.ILoginDetailService;
import com.project.libmanager.service.dto.request.UserCreateRequest;
import com.project.libmanager.service.dto.request.UserUpdateRequest;
import com.project.libmanager.service.dto.response.UserResponse;
import com.project.libmanager.entity.Role;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.mapper.UserMapper;
import com.project.libmanager.repository.RoleRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IUserService;

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

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserQueryService userQueryService;
    private final IActivityLogService activityLogService;
    private final BorrowingRepository borrowingRepository;
    private final LoginDetailRepository loginDetailRepository;
    private final ILoginDetailService loginDetailService;

    /**
     * Creates a new user and assigns a default role.
     *
     * @param request The request containing information about the user to be
     *                created.
     * @return The response containing the details of the created user.
     * @throws AppException If the user already exists or there is an error during
     *                      creation.
     * @implNote This method encrypts the user's password and assigns the default
     *           user role. The user is marked as not verified.
     */
    @Transactional
    @Override
    public UserResponse createUser(UserCreateRequest request) {
        User user = userMapper.toUser(request);
        Set<Role> roles = new HashSet<>();
        
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        roles.addAll(request.getListRole().stream()
                .map(x -> roleRepository.findByName(x)
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)))
                .collect(Collectors.toSet()));

        User userAction = getAuthenticatedUser();
        try {
            user.setRoles(roles);
            userRepository.save(user);
            UserResponse userResponse = userMapper.toUserResponse(user);
            activityLogService.logAction(
                    userAction.getId(),
                    userAction.getEmail(),
                    UserAction.ADMIN_CREATE_USER,
                    "Admin create new user with email: "+ user.getEmail(),
                    null,
                    userResponse
            );

            return userResponse;
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private User getAuthenticatedUser() {
        SecurityContext jwtContext = SecurityContextHolder.getContext();
        if (jwtContext == null || jwtContext.getAuthentication() == null ||
                !jwtContext.getAuthentication().isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        log.info("Authentication {}", jwtContext.getAuthentication().getName());

        String email = jwtContext.getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Fetches a paginated list of all users.
     *
     * @param pageable Pagination details.
     * @return A page of users.
     * @throws AppException If there is an error during retrieval.
     * @implNote This method fetches all users from the repository and returns them
     *           in a paginated format.
     */
    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        Page<User> pageUser = userRepository.findAll(pageable);
        if (pageUser.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return mapUserPageUserResponsePage(pageUser);
    }

    /**
     * Converts a page of users to a page of user responses.
     *
     * @param userPage The page of users.
     * @return A page of user responses.
     * @implNote This method maps the content of the user page to a list of user
     *           responses and returns the paginated response.
     */
    @Override
    public Page<UserResponse> mapUserPageUserResponsePage(Page<User> userPage) {
        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(user -> mapToUserResponseByMapper(user.getId())).toList();

        return new PageImpl<>(userResponses, userPage.getPageable(), userPage.getTotalElements());
    }

    /**
     * Fetches the response of a user by their ID.
     *
     * @param id The ID of the user.
     * @return The response containing the details of the user.
     * @throws AppException If the user does not exist.
     * @implNote This method retrieves a specific user by ID and returns the user
     *           response.
     */
    @Override
    public UserResponse mapToUserResponseByMapper(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    /**
     * Fetches the details of a specific user.
     *
     * @param id The ID of the user.
     * @return The response containing the details of the user.
     * @throws AppException If the user does not exist.
     * @implNote This method retrieves a specific user and returns their response.
     */
    @Override
    public UserResponse getUser(Long id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    /**
     * Fetches the details of the currently authenticated user.
     *
     * @return The response containing the details of the authenticated user.
     * @throws AppException If the user is not authenticated or does not exist.
     * @implNote This method retrieves the currently logged-in user using the
     *           security context.
     */
    @Override
    public UserResponse getMyInfo() {
        try {
            SecurityContext jwtContext = SecurityContextHolder.getContext();

            if (jwtContext == null || jwtContext.getAuthentication() == null ||
                    !jwtContext.getAuthentication().isAuthenticated()) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            String email = jwtContext.getAuthentication().getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            if (user == null) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }

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
     * @param id      The ID of the user to be updated.
     * @param request The request containing updated information about the user.
     * @return The response containing the updated user details.
     * @throws AppException If the user does not exist, the email already exists, or
     *                      an error occurs during the update.
     * @implNote This method updates the user's information, password.
     */
    @Transactional
    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        UserResponse oldeUserResponse = userMapper.toUserResponse(u);
        if (u.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_ADMIN);
        }
        User userAction = getAuthenticatedUser();
        try {
            String oldPassword = u.getPassword();
            userMapper.updateUser(u, request);

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                u.setPassword(passwordEncoder.encode(request.getPassword()));
            } else {
              u.setPassword(oldPassword);
            }

            Set<Role> roles = new HashSet<>();
            roles.addAll(request.getListRole().stream()
                    .map(x -> roleRepository.findByName(x)
                            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)))
                    .collect(Collectors.toSet()));
            u.setRoles(roles);
            User newUser = userRepository.save(u);
            UserResponse userResponse = userMapper.toUserResponse(newUser);
            activityLogService.logAction(
                    userAction.getId(),
                    userAction.getEmail(),
                    UserAction.ADMIN_UPDATE_USER,
                    "Admin update user with email: "+ u.getEmail(),
                    oldeUserResponse,
                    userResponse
            );
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
     * @param userId The ID of the user to be deleted.
     * @throws AppException If the user does not exist or there is an error during
     *                      deletion.
     * @implNote This method checks if the user has borrowings before deleting. If
     *           so, the user is marked as deleted instead of being fully deleted.
     */
    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ADMIN);
        }
        if (borrowingRepository.existsByUserIdAndReturnDateIsNull(userId)) {
            throw new AppException(ErrorCode.USER_CANNOT_BE_DELETED);
        }

        User userAction = getAuthenticatedUser();
        try {
            user.setDeleted(true);
            List<LoginDetail> userLoginDetails = loginDetailRepository.findByUserId(user.getId());
            for (LoginDetail loginDetail : userLoginDetails) {
                loginDetailService.disableLoginDetailById(loginDetail.getJti());
            }
            userRepository.save(user);
            activityLogService.logAction(
                    userAction.getId(),
                    userAction.getEmail(),
                    UserAction.ADMIN_DELETE_USER,
                    "Admin create new user with email: "+ user.getEmail(),
                    userMapper.toUserResponse(userAction),
                    null
            );
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public Page<UserResponse> searchUSer(UserCriteria criteria, Pageable pageable) {
        Page<User> users = userQueryService.findByCriteria(criteria, pageable);
        return mapUserPageUserResponsePage(users);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
