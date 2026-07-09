package com.samplus.smartrecrutare.auth;

import com.samplus.smartrecrutare.auth.config.Auth0Props;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class Auth0ControllerSmokeTest {
    private final Auth0Props properties = props();
    private final Auth0Service auth0Service = mock(Auth0Service.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new Auth0Controller(properties, auth0Service))
            .build();

    @Test
    void loginEndpointSmokeRedirectsToAuth0() throws Exception {
        MockHttpSession session = new MockHttpSession();
        when(auth0Service.createAuthorizeUrl(session, "http://localhost:8080/auth/callback"))
                .thenReturn("https://example.auth0.com/authorize?client_id=test&request_uri=urn:test");

        mockMvc.perform(get("/auth/login").session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.auth0.com/authorize?client_id=test&request_uri=urn:test"));
    }

    @Test
    void callbackEndpointSmokeStoresTokensAndReturnsToSpa() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth_vue_redirect_uri", "http://localhost:5173/auth/callback");
        when(auth0Service.exchangeCodeForTokens(eq("code-123"), eq("state-123"), same(session)))
                .thenReturn(Map.of(
                        "access_token", "access-token",
                        "token_type", "Bearer"
                ));

        mockMvc.perform(get("/auth/callback")
                        .param("code", "code-123")
                        .param("state", "state-123")
                        .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/auth/callback?login=success"));
    }

    @Test
    void meEndpointSmokeReportsAuthStateWithoutTokens() throws Exception {
        MockHttpSession authenticatedSession = new MockHttpSession();
        authenticatedSession.setAttribute("auth0_tokens", Map.of("access_token", "access-token"));

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.authenticated").value(false));

        mockMvc.perform(get("/auth/me").session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    private Auth0Props props() {
        Auth0Props props = new Auth0Props();
        props.setRedirectUri("http://localhost:8080/auth/callback");
        props.setVueRedirectUri("http://localhost:5173/auth/callback");
        return props;
    }
}
