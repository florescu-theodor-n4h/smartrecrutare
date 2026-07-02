package com.samplus.smartrecrutare.auth;


import com.samplus.smartrecrutare.auth.config.Auth0Props;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Auth0Service {
    private final Auth0Props properties;
    //@Qualifier("jarJwtEncoder") selectat automatic dupa nume
    private final JwtEncoder jarJwtEncoder;
    private final RestClient secureRestClient;


    public String createAuthorizeUrl(HttpSession session) {
        String state = randomUrlSafe();
        String nonce = randomUrlSafe();
        String codeVerifier = randomUrlSafe();
        String codeChallenge = sha256Base64Url(codeVerifier);

        session.setAttribute("oauth_state", state);
        session.setAttribute("oauth_nonce", nonce);
        session.setAttribute("pkce_code_verifier", codeVerifier);

        String jarJwt = createJarJwt(state, nonce, codeChallenge);

        String requestUri = pushJarToAuth0ParEndpoint(jarJwt);

        return UriComponentsBuilder
                .fromUriString("https://" + properties.getDomain() + "/authorize")
                .queryParam("client_id", properties.getClientId())
                .queryParam("request_uri", requestUri)
                .build()
                .toUriString();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> exchangeCodeForTokens(String code, String returnedState, HttpSession session) {
        String expectedState = (String) session.getAttribute("oauth_state");
        String codeVerifier = (String) session.getAttribute("pkce_code_verifier");

        if (expectedState == null || !expectedState.equals(returnedState)) {
            throw new IllegalStateException("Invalid OAuth state");
        }

        if (codeVerifier == null) {
            throw new IllegalStateException("Missing PKCE code verifier");
        }

        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("code", code);
        body.add("redirect_uri", properties.getRedirectUri());
        body.add("code_verifier", codeVerifier);

        Map<String, Object> tokenResponse = secureRestClient
                .post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);

        session.removeAttribute("oauth_state");
        session.removeAttribute("oauth_nonce");
        session.removeAttribute("pkce_code_verifier");

        return tokenResponse;
    }

    private String createJarJwt(String state, String nonce, String codeChallenge) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(properties.getClientId())
                .audience(List.of("https://" + properties.getDomain() + "/"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("client_id", properties.getClientId())
                .claim("response_type", "code")
                .claim("redirect_uri", properties.getRedirectUri())
                .claim("scope", "openid profile email")
                .claim("state", state)
                .claim("nonce", nonce)
                .claim("code_challenge", codeChallenge)
                .claim("code_challenge_method", "S256");

        if (properties.getAudience() != null && !properties.getAudience().isBlank()) {
            claimsBuilder.claim("audience", properties.getAudience());
        }

        JwtClaimsSet claims = claimsBuilder.build();

        JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256)
                .type("JWT")
                .keyId(properties.getJar().getKeyId())
                .build();

        return jarJwtEncoder
                .encode(JwtEncoderParameters.from(headers, claims))
                .getTokenValue();
    }

    @SuppressWarnings("unchecked")
    private String pushJarToAuth0ParEndpoint(String jarJwt) {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("request", jarJwt);

        Map<String, Object> response = secureRestClient
                .post()
                .uri("/oauth/par")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);

        assert response != null;
        Object requestUri = response.get("request_uri");

        if (requestUri == null) {
            throw new IllegalStateException("Auth0 PAR response did not contain request_uri: " + response);
        }

        return requestUri.toString();
    }

    private static String randomUrlSafe() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Base64Url(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.US_ASCII));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create PKCE code challenge", e);
        }
    }
}