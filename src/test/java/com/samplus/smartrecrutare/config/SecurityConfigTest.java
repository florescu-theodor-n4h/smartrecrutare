package com.samplus.smartrecrutare.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HTTPAccessPathsProperties accessPathsProperties;

    @Test
    void securityPropertiesExposeDefaultPublicPaths() {
        assertThat(accessPathsProperties.getPublicPaths())
                .contains("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**");
    }

    @Test
    void devAuthEndpointsChallengeAnonymousRequestsOutsideDevProfile() throws Exception {
        mockMvc.perform(get("/dev-auth/token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devAuthEndpointsAreDeniedForAuthenticatedRequestsOutsideDevProfile() throws Exception {
        mockMvc.perform(get("/dev-auth/token")
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }
}
