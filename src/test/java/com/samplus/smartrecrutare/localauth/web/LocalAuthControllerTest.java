package com.samplus.smartrecrutare.localauth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samplus.smartrecrutare.HandlerExceptiiGlobal;
import com.samplus.smartrecrutare.localauth.dto.LocalLoginRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalLoginResponse;
import com.samplus.smartrecrutare.localauth.dto.LocalRegistrationRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalUserResponse;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthConflictException;
import com.samplus.smartrecrutare.localauth.service.LocalAuthService;
import com.samplus.smartrecrutare.localauth.service.LocalRegistrationService;
import com.samplus.smartrecrutare.security.RolAplicatie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocalAuthControllerTest {
    private final LocalAuthService localAuthService = mock(LocalAuthService.class);
    private final LocalRegistrationService localRegistrationService = mock(LocalRegistrationService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new LocalAuthController(localAuthService, localRegistrationService))
            .setControllerAdvice(new HandlerExceptiiGlobal())
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loginDelegatesToServiceAndReturnsTokenResponse() throws Exception {
        LocalLoginResponse response = new LocalLoginResponse(
                "Bearer",
                "local-token",
                Instant.parse("2026-07-09T10:00:00Z"),
                localUserResponse()
        );
        when(localAuthService.login(any(LocalLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LocalLoginRequest("admin", "Parola!123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("local-token"))
                .andExpect(jsonPath("$.user.username").value("admin"));

        verify(localAuthService).login(any(LocalLoginRequest.class));
    }

    @Test
    void loginRejectsInvalidRequestBeforeCallingService() throws Exception {
        mockMvc.perform(post("/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LocalLoginRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validare esuata"));

        verifyNoInteractions(localAuthService);
    }

    @Test
    void registerDelegatesToServiceAndReturnsCreatedUser() throws Exception {
        LocalUserResponse response = localUserResponse("new-user", "new-user@example.test", Set.of(RolAplicatie.USER));
        when(localRegistrationService.register(any(LocalRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/local/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LocalRegistrationRequest(
                                "new-user",
                                "new-user@example.test",
                                "ParolaNoua!123"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new-user"))
                .andExpect(jsonPath("$.email").value("new-user@example.test"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

        verify(localRegistrationService).register(any(LocalRegistrationRequest.class));
    }

    @Test
    void registerRejectsInvalidRequestBeforeCallingService() throws Exception {
        mockMvc.perform(post("/auth/local/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LocalRegistrationRequest(
                                "",
                                "invalid",
                                "short"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validare esuata"));

        verifyNoInteractions(localRegistrationService);
    }

    @Test
    void registerMapsConflictToProblemResponse() throws Exception {
        when(localRegistrationService.register(any(LocalRegistrationRequest.class)))
                .thenThrow(new LocalAuthConflictException("Emailul local este deja folosit"));

        mockMvc.perform(post("/auth/local/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LocalRegistrationRequest(
                                "new-user",
                                "new-user@example.test",
                                "ParolaNoua!123"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict de date"))
                .andExpect(jsonPath("$.detail").value("Emailul local este deja folosit"));
    }

    @Test
    void loginMapsLocalAuthConflictExceptionToConflictProblem() throws Exception {
        when(localAuthService.login(any(LocalLoginRequest.class)))
                .thenThrow(new LocalAuthConflictException("Utilizator local duplicat"));

        mockMvc.perform(post("/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LocalLoginRequest("admin", "Parola!123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict de date"))
                .andExpect(jsonPath("$.detail").value("Utilizator local duplicat"));
    }

    @Test
    void meReturnsAuthenticatedIdentityAndAuthorities() throws Exception {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "admin",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        authentication.setAuthenticated(true);

        mockMvc.perform(get("/auth/local/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.name").value("admin"))
                .andExpect(jsonPath("$.authorities[0].authority").value("ROLE_ADMIN"));
    }

    @Test
    void meReturnsAnonymousShapeWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(get("/auth/local/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.name").value(""))
                .andExpect(jsonPath("$.authorities").isArray());
    }

    @Test
    void localAuthConflictExceptionPreservesMessage() {
        LocalAuthConflictException exception = new LocalAuthConflictException("conflict");

        assertThat(exception).hasMessage("conflict");
    }

    private LocalUserResponse localUserResponse() {
        return localUserResponse("admin", "admin@example.test", Set.of(RolAplicatie.ADMIN));
    }

    private LocalUserResponse localUserResponse(String username, String email, Set<RolAplicatie> roles) {
        Instant now = Instant.parse("2026-07-09T09:00:00Z");
        return new LocalUserResponse(
                1L,
                username,
                email,
                true,
                false,
                roles,
                Set.of(),
                now,
                now,
                "test",
                now,
                "test",
                0L
        );
    }
}
