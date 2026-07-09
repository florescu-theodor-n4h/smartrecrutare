package com.samplus.smartrecrutare.auth;

import com.samplus.smartrecrutare.auth.config.Auth0Props;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class Auth0ControllerTest {

    private final Auth0Props properties = props();
    private final Auth0Service auth0Service = mock(Auth0Service.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new Auth0Controller(properties, auth0Service))
            .build();

    @Test
    void loginRedirectsToAuthorizeUrlCreatedByService() throws Exception {
        MockHttpSession session = new MockHttpSession();
        when(auth0Service.createAuthorizeUrl(session, "http://localhost:8080/auth/callback"))
                .thenReturn("https://example.auth0.com/authorize?client_id=test&request_uri=urn:test");

        mockMvc.perform(get("/auth/login").session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.auth0.com/authorize?client_id=test&request_uri=urn:test"));

        verify(auth0Service).createAuthorizeUrl(session, "http://localhost:8080/auth/callback");
        assertThat(session.getAttribute("oauth_vue_redirect_uri")).isEqualTo("http://localhost:5173/auth/callback");
    }

    @Test
    void loginDerivesDynamicRedirectUrisFromRequestUrl() throws Exception {
        Auth0Props dynamicProperties = new Auth0Props();
        dynamicProperties.setRedirectUri("__dynamic__");
        dynamicProperties.setVueRedirectUri("__dynamic__");
        Auth0Service dynamicAuth0Service = mock(Auth0Service.class);
        MockMvc dynamicMockMvc = MockMvcBuilders
                .standaloneSetup(new Auth0Controller(dynamicProperties, dynamicAuth0Service))
                .build();
        MockHttpSession session = new MockHttpSession();
        when(dynamicAuth0Service.createAuthorizeUrl(session, "https://api.example.test/auth/callback"))
                .thenReturn("https://example.auth0.com/authorize?client_id=test&request_uri=urn:test");

        dynamicMockMvc.perform(get("/auth/login")
                        .header("Origin", "https://app.example.test")
                        .with(request -> {
                            request.setScheme("https");
                            request.setServerName("api.example.test");
                            request.setServerPort(443);
                            request.setSecure(true);
                            return request;
                        })
                        .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.auth0.com/authorize?client_id=test&request_uri=urn:test"));

        verify(dynamicAuth0Service).createAuthorizeUrl(session, "https://api.example.test/auth/callback");
        assertThat(session.getAttribute("oauth_vue_redirect_uri")).isEqualTo("https://app.example.test/auth/callback");
    }

    @Test
    void loginRedirectsBackToVueWhenAuthorizeCreationFails() throws Exception {
        Auth0Props dynamicProperties = new Auth0Props();
        dynamicProperties.setRedirectUri("__dynamic__");
        dynamicProperties.setVueRedirectUri("__dynamic__");
        Auth0Service dynamicAuth0Service = mock(Auth0Service.class);
        MockMvc dynamicMockMvc = MockMvcBuilders
                .standaloneSetup(new Auth0Controller(dynamicProperties, dynamicAuth0Service))
                .build();
        MockHttpSession session = new MockHttpSession();
        when(dynamicAuth0Service.createAuthorizeUrl(session, "https://api.example.test/auth/callback"))
                .thenThrow(Auth0OAuthException.upstreamUnavailable("PAR request", "connection refused"));

        dynamicMockMvc.perform(get("/auth/login")
                        .header("Origin", "https://app.example.test")
                        .with(request -> {
                            request.setScheme("https");
                            request.setServerName("api.example.test");
                            request.setServerPort(443);
                            request.setSecure(true);
                            return request;
                        })
                        .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://app.example.test/auth/callback?login=error&error=auth0_authorize_failed"));
    }

    @Test
    void callbackStoresTokensAndRedirectsToVueSuccessUrl() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<String, Object> tokens = Map.of(
                "access_token", "secret-access-token",
                "id_token", "secret-id-token",
                "token_type", "Bearer"
        );
        session.setAttribute("oauth_vue_redirect_uri", "http://localhost:5173/auth/callback");
        when(auth0Service.exchangeCodeForTokens(eq("code-123"), eq("state-123"), same(session)))
                .thenReturn(tokens);

        mockMvc.perform(get("/auth/callback")
                        .param("code", "code-123")
                        .param("state", "state-123")
                        .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/auth/callback?login=success"));

        verify(auth0Service).exchangeCodeForTokens("code-123", "state-123", session);
        assertThat(session.getAttribute("auth0_tokens")).isEqualTo(tokens);
    }

    @Test
    void callbackRedirectsToVueErrorUrlWhenAuth0ReturnsError() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth_vue_redirect_uri", "http://localhost:5173/auth/callback");

        mockMvc.perform(get("/auth/callback")
                        .param("error", "access_denied")
                        .param("state", "state-123")
                        .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/auth/callback?login=error&error=access_denied"));
    }

    @Test
    void callbackRedirectsToVueErrorUrlWhenTokenExchangeFails() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth_vue_redirect_uri", "http://localhost:5173/auth/callback");
        when(auth0Service.exchangeCodeForTokens(eq("code-123"), eq("state-123"), same(session)))
                .thenThrow(Auth0OAuthException.upstream("token exchange", 400, "{\"error\":\"invalid_grant\"}"));

        mockMvc.perform(get("/auth/callback")
                        .param("code", "code-123")
                        .param("state", "state-123")
                        .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/auth/callback?login=error&error=auth0_token_failed"));
    }

    @Test
    void meReturnsUnauthorizedWhenSessionHasNoTokens() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void meDoesNotExposeRawTokensWhenSessionIsAuthenticated() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("auth0_tokens", Map.of(
                "access_token", "secret-access-token",
                "id_token", "secret-id-token",
                "refresh_token", "secret-refresh-token"
        ));

        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$", not(hasKey("tokens"))))
                .andExpect(jsonPath("$", not(hasKey("access_token"))))
                .andExpect(jsonPath("$", not(hasKey("id_token"))))
                .andExpect(jsonPath("$", not(hasKey("refresh_token"))));
    }

    private Auth0Props props() {
        Auth0Props props = new Auth0Props();
        props.setRedirectUri("http://localhost:8080/auth/callback");
        props.setVueRedirectUri("http://localhost:5173/auth/callback");
        return props;
    }
}
