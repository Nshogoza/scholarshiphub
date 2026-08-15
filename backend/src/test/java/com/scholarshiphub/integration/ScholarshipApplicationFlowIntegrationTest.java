package com.scholarshiphub.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.service.MailService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end happy path: student registers and verifies, admin publishes a
 * scholarship, the student applies with a document and submits, admin
 * assigns a reviewer, and the reviewer approves -- asserting the visible
 * status transition at each step. Exercises all three roles through the
 * real HTTP layer against a real (containerized) Postgres.
 */
class ScholarshipApplicationFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private MailService mailService;

    @Test
    void fullApplicationLifecycle_fromRegistrationToApproval() throws Exception {
        doNothing().when(mailService).sendApplicationStatusChangedEmail(any(), any(), any());
        doNothing().when(mailService).sendReviewerAssignedEmail(any(), any(), any());

        // 1. Student registers.
        String studentEmail = "flow-student@example.com";
        Map<String, Object> registerBody = Map.of(
                "email", studentEmail,
                "password", "Str0ng!Passw0rd",
                "firstName", "Flo",
                "lastName", "Student",
                "phone", "");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());

        // 2. Capture and use the verification token that would have been emailed.
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(mailService).sendVerificationEmail(any(User.class), tokenCaptor.capture());
        mockMvc.perform(get("/api/v1/auth/verify-email").param("token", tokenCaptor.getValue()))
                .andExpect(status().isOk());

        // 3. Student logs in.
        String studentToken = loginAndGetAccessToken(studentEmail, "Str0ng!Passw0rd");

        // 4. Admin (seeded) logs in and publishes a new scholarship.
        String adminToken = loginAndGetAccessToken("admin@scholarshiphub.com", "Admin@12345");
        Map<String, Object> scholarshipBody = Map.of(
                "title", "Flow Test Scholarship",
                "description", "Covers the full application lifecycle.",
                "eligibilityCriteria", "Any enrolled student",
                "amount", 1000,
                "applicationDeadline", Instant.now().plus(30, ChronoUnit.DAYS).toString(),
                "requiredDocuments", java.util.List.of(Map.of("documentName", "Transcript", "mandatory", true)));
        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/scholarships")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scholarshipBody)))
                .andExpect(status().isCreated())
                .andReturn();
        Long scholarshipId = ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id"))
                .longValue();

        mockMvc.perform(patch("/api/v1/admin/scholarships/{id}/status", scholarshipId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk());

        // 5. Student creates a draft application, uploads the required document, and submits.
        MvcResult appResult = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scholarshipId\":" + scholarshipId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        Long applicationId = ((Number) JsonPath.read(appResult.getResponse().getContentAsString(), "$.data.id"))
                .longValue();

        MockMultipartFile transcript = new MockMultipartFile(
                "file", "transcript.pdf", "application/pdf", "dummy pdf content".getBytes());
        mockMvc.perform(multipart("/api/v1/applications/{id}/documents", applicationId)
                        .file(transcript)
                        .param("documentName", "Transcript")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/applications/{id}/submit", applicationId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        // 6. Admin assigns the seeded reviewer.
        User reviewer = userRepository.findByEmailIgnoreCase("reviewer@scholarshiphub.com").orElseThrow();
        mockMvc.perform(patch("/api/v1/admin/applications/{id}/assign-reviewer", applicationId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewerId\":" + reviewer.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        // 7. Reviewer approves the application.
        String reviewerToken = loginAndGetAccessToken("reviewer@scholarshiphub.com", "Reviewer@12345");
        mockMvc.perform(post("/api/v1/reviewer/applications/{id}/reviews", applicationId)
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":92,\"comments\":\"Strong candidate\",\"recommendation\":\"APPROVE\"}"))
                .andExpect(status().isCreated());

        // 8. Student sees the application as APPROVED.
        mockMvc.perform(get("/api/v1/applications/{id}", applicationId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
        assertThat(token).isNotBlank();
        return token;
    }
}
