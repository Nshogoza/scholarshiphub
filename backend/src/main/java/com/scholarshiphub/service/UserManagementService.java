package com.scholarshiphub.service;

import com.scholarshiphub.dto.request.CreateUserRequest;
import com.scholarshiphub.dto.response.UserSummaryResponse;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Administrator-only account management (creating reviewer/admin accounts,
 *  browsing all users, activating/locking/disabling accounts). */
public interface UserManagementService {

    Page<UserSummaryResponse> listUsers(RoleName role, UserStatus status, String search, Pageable pageable);

    UserSummaryResponse getUser(Long userId);

    UserSummaryResponse createUser(CreateUserRequest request, Long actorAdminId);

    UserSummaryResponse updateStatus(Long userId, UserStatus newStatus, Long actorAdminId);
}
