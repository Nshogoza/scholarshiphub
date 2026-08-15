package com.scholarshiphub.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.scholarshiphub.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Verifies the RBAC boundaries the whole platform depends on: every
 * protected resource must reject unauthenticated callers with 401, and
 * reject an authenticated user of the wrong role with 403.
 */
class RoleAccessIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void adminEndpoints_rejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/analytics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentToken_cannotAccessAdminOrReviewerEndpoints() throws Exception {
        String studentEmail = "rbac-student@example.com";
        registerAndVerify(studentEmail);
        String studentToken = login(studentEmail, "Str0ng!Passw0rd");

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/reviewer/applications").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminToken_canAccessAdminEndpoints() throws Exception {
        String adminToken = login("admin@scholarshiphub.com", "Admin@12345");

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    void registerAndVerify(String email) throws Exception {
        Map<String, Object> body = Map.of(
                "email", email, "password", "Str0ng!Passw0rd",
                "firstName", "RBAC", "lastName", "Tester", "phone", "");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // This test only needs a valid token to probe RBAC boundaries, so the
        // email-verification round trip itself (covered by
        // ScholarshipApplicationFlowIntegrationTest) is bypassed by flipping the
        // flag directly in the database rather than parsing a captured token.
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }
}
