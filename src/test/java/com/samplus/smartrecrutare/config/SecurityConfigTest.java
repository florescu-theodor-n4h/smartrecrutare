package com.samplus.smartrecrutare.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {
    private static final String BASIC_CHALLENGE = "Basic realm=\"User Visible Realm\", charset=\"UTF-8\"";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HTTPAccessPathsProperties accessPathsProperties;

    @Test
    void securityPropertiesExposeDefaultPublicPaths() {
        assertThat(accessPathsProperties.getPublicPaths())
                .contains(
                        "/",
                        "/index.html",
                        "/favicon.ico",
                        "/assets/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/images/**",
                        "/static/**",
                        "/manifest.webmanifest",
                        "/robots.txt",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                );
    }

    @Test
    void localRegistrationEndpointIsReachableWithoutJwt() throws Exception {
        mockMvc.perform(post("/auth/local/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void devAuthEndpointsChallengeAnonymousRequestsOutsideDevProfile() throws Exception {
        mockMvc.perform(get("/dev-auth/token"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, BASIC_CHALLENGE));
    }

    @Test
    void devAuthTokenEndpointAcceptsConfiguredBasicCredentials() throws Exception {
        mockMvc.perform(get("/dev-auth/token")
                        .with(httpBasic("dev", "dev")))
                .andExpect(status().isOk());
    }
}
