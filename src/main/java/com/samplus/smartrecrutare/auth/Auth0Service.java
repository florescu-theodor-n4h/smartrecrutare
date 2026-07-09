package com.samplus.smartrecrutare.auth;


import com.samplus.smartrecrutare.auth.config.Auth0Props;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Serviciu pentru fluxul OAuth2/Auth0 folosit de SPA.
 * Gestioneaza state, nonce, PKCE, JAR/PAR cand clientul Auth0 permite acest lucru si fallback standard
 * authorization-code + PKCE pentru clienti publici. Tokenurile si secretele nu sunt scrise in loguri.
 */
@Service
@RequiredArgsConstructor
public class Auth0Service {
    static final String OAUTH_REDIRECT_URI = "oauth_redirect_uri";
    static final String OAUTH_PUBLIC_CLIENT = "oauth_public_client";
    static final String OAUTH_STATE = "oauth_state";
    static final String OAUTH_NONCE = "oauth_nonce";
    static final String PKCE_CODE_VERIFIER = "pkce_code_verifier";

    private static final String AUTHORIZATION_CODE_GRANT = "authorization_code";
    private static final String SCOPE = "openid profile email";
    private static final int TOKEN_MIN_SECONDS = 300;
    private static final int PAR_MAX_ATTEMPTS = 2;

    private static final Logger log = LoggerFactory.getLogger(Auth0Service.class);

    private final Auth0Props properties;
    //@Qualifier("jarJwtEncoder") selectat automatic dupa nume
    private final JwtEncoder jarJwtEncoder;
    private final RestClient secureRestClient;


    /**
     * Creeaza URL-ul de autorizare Auth0 si salveaza in sesiune starea temporara OAuth.
     * Preferinta este JAR + PAR; cand Auth0 indica un client public, se foloseste fallback PKCE standard.
     */
    public String createAuthorizeUrl(HttpSession session, String redirectUri) {
        validateLocalAuth0Configuration();
        validateRedirectUri(redirectUri);

        String state = randomUrlSafe();
        String nonce = randomUrlSafe();
        String codeVerifier = randomUrlSafe();
        String codeChallenge = sha256Base64Url(codeVerifier);

        session.setAttribute(OAUTH_STATE, state);
        session.setAttribute(OAUTH_NONCE, nonce);
        session.setAttribute(PKCE_CODE_VERIFIER, codeVerifier);
        session.setAttribute(OAUTH_REDIRECT_URI, redirectUri);
        session.setAttribute(OAUTH_PUBLIC_CLIENT, false);

        log.info("Starting Auth0 authorization flow session={} redirectUri={} publicClientFallback=false",
                safeSessionId(session),
                redirectUri
        );

        if (!hasClientSecret()) {
            session.setAttribute(OAUTH_PUBLIC_CLIENT, true);
            log.info("Auth0 client secret is not configured; using authorization-code PKCE public-client flow session={}",
                    safeSessionId(session)
            );
            return createPlainPkceAuthorizeUrl(state, nonce, codeChallenge, redirectUri);
        }

        String jarJwt = createJarJwt(state, nonce, codeChallenge, redirectUri);

        String requestUri;
        try {
            requestUri = pushJarToAuth0ParEndpoint(jarJwt);
        } catch (Auth0OAuthException exception) {
            if (!isPublicClientParUnsupported(exception)) {
                throw exception;
            }

            session.setAttribute(OAUTH_PUBLIC_CLIENT, true);
            log.warn("Auth0 public client rejected PAR; falling back to authorization-code PKCE URL session={}",
                    safeSessionId(session)
            );
            return createPlainPkceAuthorizeUrl(state, nonce, codeChallenge, redirectUri);
        }

        log.info("Auth0 PAR accepted session={} requestUriPresent=true", safeSessionId(session));

        return UriComponentsBuilder
                .fromUriString("https://" + properties.getDomain() + "/authorize")
                .queryParam("client_id", properties.getClientId())
                .queryParam("request_uri", requestUri)
                .build()
                .encode()
                .toUriString();
    }

    /**
     * Curata doar atributele temporare ale fluxului OAuth, fara a sterge tokenurile deja autentificate.
     */
    public void clearTransientOauthSessionState(HttpSession session) {
        session.removeAttribute(OAUTH_STATE);
        session.removeAttribute(OAUTH_NONCE);
        session.removeAttribute(PKCE_CODE_VERIFIER);
        session.removeAttribute(OAUTH_REDIRECT_URI);
        session.removeAttribute(OAUTH_PUBLIC_CLIENT);
    }

    /**
     * Valideaza state/PKCE si schimba codul Auth0 pe tokenuri.
     * Token exchange nu este retry-uit deoarece codul OAuth poate fi de unica folosinta.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> exchangeCodeForTokens(String code, String returnedState, HttpSession session) {
        validateLocalAuth0Configuration();
        validateAuthorizationCode(code);

        String expectedState = (String) session.getAttribute(OAUTH_STATE);
        String codeVerifier = (String) session.getAttribute(PKCE_CODE_VERIFIER);
        String redirectUri = (String) session.getAttribute(OAUTH_REDIRECT_URI);
        boolean publicClient = Boolean.TRUE.equals(session.getAttribute(OAUTH_PUBLIC_CLIENT));

        if (expectedState == null || !expectedState.equals(returnedState)) {
            log.warn("Rejected Auth0 callback with invalid state session={} expectedStatePresent={} returnedStatePresent={}",
                    safeSessionId(session),
                    expectedState != null,
                    returnedState != null && !returnedState.isBlank()
            );
            throw Auth0OAuthException.badRequest("Invalid OAuth state");
        }

        if (codeVerifier == null || codeVerifier.isBlank()) {
            log.warn("Rejected Auth0 callback with missing PKCE verifier session={}", safeSessionId(session));
            throw Auth0OAuthException.badRequest("Missing PKCE code verifier");
        }

        if (redirectUri == null || redirectUri.isBlank()) {
            log.warn("Rejected Auth0 callback with missing redirect URI session={}", safeSessionId(session));
            throw Auth0OAuthException.badRequest("Missing OAuth redirect URI");
        }

        validateRedirectUri(redirectUri);

        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", AUTHORIZATION_CODE_GRANT);
        body.add("client_id", properties.getClientId());
        addClientSecretIfConfigured(body, session);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("code_verifier", codeVerifier);

        log.info("Exchanging Auth0 authorization code session={} redirectUri={} publicClient={}",
                safeSessionId(session),
                redirectUri,
                publicClient
        );

        Map<String, Object> tokenResponse = postFormForMap("/oauth/token", body, "token exchange", false);
        validateTokenResponse(tokenResponse);
        clearTransientOauthSessionState(session);

        log.info("Auth0 token exchange succeeded session={} tokenType={} accessTokenPresent={} idTokenPresent={}",
                safeSessionId(session),
                tokenResponse.get("token_type"),
                tokenResponse.get("access_token") instanceof String,
                tokenResponse.get("id_token") instanceof String
        );

        return tokenResponse;
    }

    private String createPlainPkceAuthorizeUrl(String state, String nonce, String codeChallenge, String redirectUri) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("https://" + properties.getDomain() + "/authorize")
                .queryParam("client_id", properties.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", SCOPE)
                .queryParam("state", state)
                .queryParam("nonce", nonce)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256");

        if (properties.getAudience() != null && !properties.getAudience().isBlank()) {
            builder.queryParam("audience", properties.getAudience());
        }

        return builder.build().encode().toUriString();
    }

    private String createJarJwt(String state, String nonce, String codeChallenge, String redirectUri) {
        if (properties.getJar().getKeyId() == null || properties.getJar().getKeyId().isBlank()) {
            throw Auth0OAuthException.badRequest("Missing Auth0 JAR key ID");
        }

        Instant now = Instant.now();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(properties.getClientId())
                .audience(Arrays.asList("https://" + properties.getDomain() + "/"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(TOKEN_MIN_SECONDS))
                .claim("client_id", properties.getClientId())
                .claim("response_type", "code")
                .claim("redirect_uri", redirectUri)
                .claim("scope", SCOPE)
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

        try {
            return jarJwtEncoder
                    .encode(JwtEncoderParameters.from(headers, claims))
                    .getTokenValue();
        } catch (JwtEncodingException exception) {
            log.error("Could not sign Auth0 JAR JWT kid={} domain={}",
                    properties.getJar().getKeyId(),
                    properties.getDomain(),
                    exception
            );
            throw Auth0OAuthException.badRequest("Could not sign Auth0 authorization request");
        }
    }

    private String pushJarToAuth0ParEndpoint(String jarJwt) {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", properties.getClientId());
        addClientSecretIfConfigured(body, null);
        body.add("request", jarJwt);

        Map<String, Object> response = postFormForMap("/oauth/par", body, "PAR request", true);

        if (response == null) {
            throw Auth0OAuthException.upstreamUnavailable("PAR request", "empty response");
        }

        Object requestUri = response.get("request_uri");
        if (!(requestUri instanceof String requestUriValue) || requestUriValue.isBlank()) {
            log.warn("Auth0 PAR response missing request_uri keys={}", response.keySet());
            throw Auth0OAuthException.upstreamUnavailable("PAR request", "missing request_uri");
        }

        return requestUriValue;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postFormForMap(
            String uri,
            LinkedMultiValueMap<String, String> body,
            String operation,
            boolean retryTransient
    ) {
        int attempts = retryTransient ? PAR_MAX_ATTEMPTS : 1;
        Auth0OAuthException lastFailure = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                log.debug("Calling Auth0 {} endpoint={} attempt={} params={}",
                        operation,
                        uri,
                        attempt,
                        safeBodyKeys(body)
                );
                return secureRestClient
                        .post()
                        .uri(uri)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(body)
                        .retrieve()
                        .body(Map.class);
            } catch (Auth0OAuthException exception) {
                lastFailure = exception;
            } catch (RestClientResponseException exception) {
                lastFailure = Auth0OAuthException.upstream(
                        operation,
                        exception.getStatusCode().value(),
                        exception.getResponseBodyAsString()
                );
            } catch (RestClientException exception) {
                lastFailure = Auth0OAuthException.upstreamUnavailable(operation, exception.getMessage());
            }

            log.warn("Auth0 {} failed attempt={} retryable={} message={}",
                    operation,
                    attempt,
                    retryTransient && isTransient(lastFailure) && attempt < attempts,
                    lastFailure.getMessage()
            );

            if (!retryTransient || !isTransient(lastFailure) || attempt == attempts) {
                throw lastFailure;
            }
        }

        throw lastFailure;
    }

    private void addClientSecretIfConfigured(LinkedMultiValueMap<String, String> body, HttpSession session) {
        if (session != null && Boolean.TRUE.equals(session.getAttribute(OAUTH_PUBLIC_CLIENT))) {
            log.debug("Omitting client_secret for Auth0 public client token exchange");
            return;
        }

        if (hasClientSecret()) {
            body.add("client_secret", properties.getClientSecret());
        }
    }

    private boolean isPublicClientParUnsupported(Auth0OAuthException exception) {
        return exception.getAuth0Status() != null
                && exception.getAuth0Status() == 400
                && exception.getAuth0Body() != null
                && exception.getAuth0Body().contains("Public clients are not presently supported on the pushed_authorization_endpoint");
    }

    private boolean isTransient(Auth0OAuthException exception) {
        Integer status = exception.getAuth0Status();
        return status == null || status == 429 || status >= 500;
    }

    private void validateLocalAuth0Configuration() {
        if (properties.getDomain() == null || properties.getDomain().isBlank()) {
            throw Auth0OAuthException.badRequest("Missing Auth0 domain");
        }

        if (!properties.getDomain().endsWith(".auth0.com")) {
            throw Auth0OAuthException.badRequest("Unexpected Auth0 domain");
        }

        if (properties.getClientId() == null || properties.getClientId().isBlank()) {
            throw Auth0OAuthException.badRequest("Missing Auth0 client ID");
        }

    }

    private void validateRedirectUri(String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank()) {
            throw Auth0OAuthException.badRequest("Missing OAuth redirect URI");
        }

        try {
            java.net.URI uri = java.net.URI.create(redirectUri);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw Auth0OAuthException.badRequest("OAuth redirect URI must use http or https");
            }

            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw Auth0OAuthException.badRequest("OAuth redirect URI must include a host");
            }

            if (uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw Auth0OAuthException.badRequest("OAuth redirect URI must not include user info or fragment");
            }
        } catch (IllegalArgumentException exception) {
            throw Auth0OAuthException.badRequest("Invalid OAuth redirect URI");
        }
    }

    private void validateAuthorizationCode(String code) {
        if (code == null || code.isBlank()) {
            throw Auth0OAuthException.badRequest("Missing authorization code");
        }
    }

    private void validateTokenResponse(Map<String, Object> tokenResponse) {
        if (tokenResponse == null) {
            throw Auth0OAuthException.upstreamUnavailable("token exchange", "empty response");
        }

        Object accessToken = tokenResponse.get("access_token");
        Object tokenType = tokenResponse.get("token_type");
        if (!(accessToken instanceof String token) || token.isBlank()) {
            throw Auth0OAuthException.upstreamUnavailable("token exchange", "missing access_token");
        }

        if (!(tokenType instanceof String type) || !"Bearer".equalsIgnoreCase(type)) {
            throw Auth0OAuthException.upstreamUnavailable("token exchange", "missing Bearer token_type");
        }
    }

    private String safeSessionId(HttpSession session) {
        return session.getId() == null ? "unknown" : Integer.toHexString(session.getId().hashCode());
    }

    private String safeBodyKeys(LinkedMultiValueMap<String, String> body) {
        return body.keySet().toString();
    }

    private boolean hasClientSecret() {
        return properties.getClientSecret() != null && !properties.getClientSecret().isBlank();
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
