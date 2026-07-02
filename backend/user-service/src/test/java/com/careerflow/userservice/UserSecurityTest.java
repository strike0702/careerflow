package com.careerflow.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UserSecurityTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCanAccessOwnProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                .header("X-Request-ID", "user-me-request-id")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                    .jwt(jwt -> jwt.tokenValue("user-a").subject("user-a")
                        .claim("email", "user-a@careerflow.com")
                        .claim("given_name", "Test")
                        .claim("family_name", "User"))))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Request-ID", "user-me-request-id"))
            .andExpect(jsonPath("$.id").value("user-a"));
    }
}
