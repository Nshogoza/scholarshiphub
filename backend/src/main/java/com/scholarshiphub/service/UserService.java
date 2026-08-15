package com.scholarshiphub.service;

import com.scholarshiphub.dto.request.UpdateProfileRequest;
import com.scholarshiphub.dto.request.UpdateStudentProfileRequest;
import com.scholarshiphub.dto.response.StudentProfileResponse;
import com.scholarshiphub.dto.response.UserSummaryResponse;

/** Self-service operations any authenticated user performs on their own account. */
public interface UserService {

    UserSummaryResponse getCurrentUser(Long userId);

    UserSummaryResponse updateProfile(Long userId, UpdateProfileRequest request);

    StudentProfileResponse getStudentProfile(Long userId);

    StudentProfileResponse updateStudentProfile(Long userId, UpdateStudentProfileRequest request);
}
