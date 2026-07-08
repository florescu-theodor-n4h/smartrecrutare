package com.samplus.smartrecrutare.auth;

import com.samplus.smartrecrutare.auth.config.Auth0Props;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

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
        when(auth0Service.createAuthorizeUrl(session))
                .thenReturn("https://example.auth0.com/authorize?client_id=test&request_uri=urn:test");

        mockMvc.perform(get("/auth/login").session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.auth0.com/authorize?client_id=test&request_uri=urn:test"));

        verify(auth0Service).createAuthorizeUrl(session);
    }

    @Test
    void callbackStoresTokensAndRedirectsToVueSuccessUrl() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<String, Object> tokens = Map.of(
                "access_token", "secret-access-token",
                "id_token", "secret-id-token",
                "token_type", "Bearer"
        );
        when(auth0Service.exchangeCodeForTokens(eq("code-123"), eq("state-123"), same(session)))
                .thenReturn(tokens);

        mockMvc.perform(get("/auth/callback")
                        .param("code", "code-123")
                        .param("state", "state-123")
                        .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/auth/callback?login=success"));

        verify(auth0Service).exchangeCodeForTokens("code-123", "state-123", session);
        org.assertj.core.api.Assertions.assertThat(session.getAttribute("auth0_tokens")).isEqualTo(tokens);
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
        props.setVueRedirectUri("http://localhost:5173/auth/callback");
        return props;
    }
}
