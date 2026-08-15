package com.scholarshiphub.service.impl;

import com.scholarshiphub.dto.request.UpdateProfileRequest;
import com.scholarshiphub.dto.request.UpdateStudentProfileRequest;
import com.scholarshiphub.dto.response.StudentProfileResponse;
import com.scholarshiphub.dto.response.UserSummaryResponse;
import com.scholarshiphub.entity.StudentProfile;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.exception.ResourceNotFoundException;
import com.scholarshiphub.mapper.StudentProfileMapper;
import com.scholarshiphub.mapper.UserMapper;
import com.scholarshiphub.repository.StudentProfileRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserMapper userMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser(Long userId) {
        return userMapper.toSummary(findUser(userId));
    }

    @Override
    @Transactional
    public UserSummaryResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        userRepository.save(user);
        auditLogService.record(userId, "PROFILE_UPDATED", "User", userId, "User updated their own profile");
        return userMapper.toSummary(user);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getStudentProfile(Long userId) {
        return studentProfileMapper.toResponse(findStudentProfile(userId));
    }

    @Override
    @Transactional
    public StudentProfileResponse updateStudentProfile(Long userId, UpdateStudentProfileRequest request) {
        StudentProfile profile = findStudentProfile(userId);
        profile.setEducationLevel(request.educationLevel());
        profile.setSchool(request.school());
        profile.setGpa(request.gpa());
        profile.setPersonalStatement(request.personalStatement());
        studentProfileRepository.save(profile);
        auditLogService.record(userId, "STUDENT_PROFILE_UPDATED", "StudentProfile", profile.getId(),
                "Student updated their academic profile");
        return studentProfileMapper.toResponse(profile);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private StudentProfile findStudentProfile(Long userId) {
        return studentProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile for user", userId));
    }
}
