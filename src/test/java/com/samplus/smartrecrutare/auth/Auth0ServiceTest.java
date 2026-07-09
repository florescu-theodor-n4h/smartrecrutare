package com.samplus.smartrecrutare.auth;

import com.samplus.smartrecrutare.auth.config.Auth0Props;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.http.HttpMethod.POST;

class Auth0ServiceTest {

    private Auth0Props properties;
    private JwtEncoder jarJwtEncoder;
    private MockRestServiceServer server;
    private Auth0Service service;

    @BeforeEach
    void setUp() {
        properties = props();
        jarJwtEncoder = mock(JwtEncoder.class);
        when(jarJwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt("signed-jar"));

        RestClient.Builder builder = RestClient.builder().baseUrl("https://unit-test.auth0.com");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new Auth0Service(properties, jarJwtEncoder, builder.build());
    }

    @Test
    void createAuthorizeUrlUsesParAndStoresOauthSessionState() {
        MockHttpSession session = new MockHttpSession();
        server.expect(requestTo("https://unit-test.auth0.com/oauth/par"))
                .andExpect(method(POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(containsString("client_id=client-test")))
                .andExpect(content().string(containsString("request=signed-jar")))
                .andRespond(withSuccess("{\"request_uri\":\"urn:auth0:par:test-request\"}", MediaType.APPLICATION_JSON));

        String authorizeUrl = service.createAuthorizeUrl(session, "https://app.example.test/auth/callback");

        assertThat(authorizeUrl)
                .startsWith("https://unit-test.auth0.com/authorize")
                .contains("client_id=client-test")
                .contains("request_uri=urn:auth0:par:test-request")
                .doesNotContain("client-secret-test")
                .doesNotContain("pkce_code_verifier");
        assertThat(session.getAttribute("oauth_state")).isInstanceOf(String.class);
        assertThat(session.getAttribute("oauth_nonce")).isInstanceOf(String.class);
        assertThat(session.getAttribute("pkce_code_verifier")).isInstanceOf(String.class);
        assertThat(session.getAttribute("oauth_redirect_uri")).isEqualTo("https://app.example.test/auth/callback");
        server.verify();
    }

    @Test
    void exchangeCodeForTokensRejectsInvalidStateBeforeCallingAuth0() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth_state", "expected-state");
        session.setAttribute("pkce_code_verifier", "verifier");
        session.setAttribute("oauth_redirect_uri", "http://localhost:8080/auth/callback");

        assertThatThrownBy(() -> service.exchangeCodeForTokens("code", "wrong-state", session))
                .isInstanceOf(Auth0OAuthException.class)
                .hasMessage("Invalid OAuth state");

        server.verify();
    }

    @Test
    void exchangeCodeForTokensRejectsMissingPkceVerifierBeforeCallingAuth0() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth_state", "expected-state");
        session.setAttribute("oauth_redirect_uri", "http://localhost:8080/auth/callback");

        assertThatThrownBy(() -> service.exchangeCodeForTokens("code", "expected-state", session))
                .isInstanceOf(Auth0OAuthException.class)
                .hasMessage("Missing PKCE code verifier");

        server.verify();
    }

    @Test
    void exchangeCodeForTokensRejectsMissingRedirectUriBeforeCallingAuth0() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth_state", "expected-state");
        session.setAttribute("pkce_code_verifier", "verifier");

        assertThatThrownBy(() -> service.exchangeCodeForTokens("code", "expected-state", session))
                .isInstanceOf(Auth0OAuthException.class)
                .hasMessage("Missing OAuth redirect URI");

        server.verify();
    }

    @Test
    void createAuthorizeUrlIncludesAuth0BadRequestBodyInException() {
        MockHttpSession session = new MockHttpSession();
        server.expect(requestTo("https://unit-test.auth0.com/oauth/par"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_request\",\"error_description\":\"redirect_uri is invalid\"}"));

        assertThatThrownBy(() -> service.createAuthorizeUrl(session, "https://app.example.test/auth/callback"))
                .isInstanceOf(Auth0OAuthException.class)
                .hasMessageContaining("Auth0 PAR request failed with status 400")
                .hasMessageContaining("redirect_uri is invalid");

        server.verify();
    }

    @Test
    void exchangeCodeForTokensPostsAuthorizationCodeAndClearsOauthSessionState() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("oauth_state", "expected-state");
        session.setAttribute("oauth_nonce", "nonce");
        session.setAttribute("pkce_code_verifier", "verifier-123");
        session.setAttribute("oauth_redirect_uri", "https://app.example.test/auth/callback");
        server.expect(requestTo("https://unit-test.auth0.com/oauth/token"))
                .andExpect(method(POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(containsString("grant_type=authorization_code")))
                .andExpect(content().string(containsString("client_id=client-test")))
                .andExpect(content().string(containsString("code=code-123")))
                .andExpect(content().string(containsString("redirect_uri=https%3A%2F%2Fapp.example.test%2Fauth%2Fcallback")))
                .andExpect(content().string(containsString("code_verifier=verifier-123")))
                .andRespond(withSuccess("""
                        {
                          "access_token": "access-token",
                          "id_token": "id-token",
                          "token_type": "Bearer",
                          "expires_in": 3600
                        }
                        """, MediaType.APPLICATION_JSON));

        Map<String, Object> tokens = service.exchangeCodeForTokens("code-123", "expected-state", session);

        assertThat(tokens)
                .containsEntry("access_token", "access-token")
                .containsEntry("id_token", "id-token")
                .containsEntry("token_type", "Bearer");
        assertThat(session.getAttribute("oauth_state")).isNull();
        assertThat(session.getAttribute("oauth_nonce")).isNull();
        assertThat(session.getAttribute("pkce_code_verifier")).isNull();
        assertThat(session.getAttribute("oauth_redirect_uri")).isNull();
        server.verify();
    }

    private Auth0Props props() {
        Auth0Props props = new Auth0Props();
        props.setDomain("unit-test.auth0.com");
        props.setClientId("client-test");
        props.setClientSecret("client-secret-test");
        props.setAudience("https://api.example.test");
        props.setRedirectUri("__dynamic__");
        props.setVueRedirectUri("__dynamic__");
        props.getJar().setKeyId("kid-test");
        return props;
    }

    private Jwt jwt(String tokenValue) {
        Instant now = Instant.now();
        return new Jwt(
                tokenValue,
                now,
                now.plusSeconds(300),
                Map.of("alg", "RS256", "kid", "kid-test"),
                Map.of("sub", "jar")
        );
    }
}
