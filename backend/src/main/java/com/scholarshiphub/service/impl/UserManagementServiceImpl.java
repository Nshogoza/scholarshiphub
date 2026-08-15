package com.scholarshiphub.service.impl;

import com.scholarshiphub.dto.request.CreateUserRequest;
import com.scholarshiphub.dto.response.UserSummaryResponse;
import com.scholarshiphub.entity.Role;
import com.scholarshiphub.entity.StudentProfile;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.entity.enums.UserStatus;
import com.scholarshiphub.exception.DuplicateResourceException;
import com.scholarshiphub.exception.ResourceNotFoundException;
import com.scholarshiphub.mapper.UserMapper;
import com.scholarshiphub.repository.RoleRepository;
import com.scholarshiphub.repository.StudentProfileRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.repository.specification.UserSpecifications;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> listUsers(RoleName role, UserStatus status, String search, Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecifications.hasRole(role))
                .and(UserSpecifications.hasStatus(status))
                .and(UserSpecifications.emailContains(search));
        return userRepository.findAll(spec, pageable).map(userMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getUser(Long userId) {
        return userMapper.toSummary(findUser(userId));
    }

    @Override
    @Transactional
    public UserSummaryResponse createUser(CreateUserRequest request, Long actorAdminId) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new IllegalStateException("Role missing -- check Flyway seed data: " + request.role()));

        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true) // admin-provisioned accounts are trusted immediately
                .build();
        user = userRepository.save(user);

        if (request.role() == RoleName.STUDENT) {
            studentProfileRepository.save(StudentProfile.builder().user(user).build());
        }

        auditLogService.record(actorAdminId, "USER_CREATED_BY_ADMIN", "User", user.getId(),
                "Admin created " + request.role() + " account for " + user.getEmail());

        return userMapper.toSummary(user);
    }

    @Override
    @Transactional
    public UserSummaryResponse updateStatus(Long userId, UserStatus newStatus, Long actorAdminId) {
        User user = findUser(userId);
        user.setStatus(newStatus);
        if (newStatus == UserStatus.ACTIVE) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }
        userRepository.save(user);

        auditLogService.record(actorAdminId, "USER_STATUS_CHANGED", "User", user.getId(),
                "Admin set status of " + user.getEmail() + " to " + newStatus);

        return userMapper.toSummary(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
