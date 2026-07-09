package com.samplus.smartrecrutare.auth;

import com.samplus.smartrecrutare.auth.config.Auth0Props;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * Controllerul principal de autentificare.
 * Aplicatia Vue.JS (SPA) se autentifica folosind JWT.
 * - PAR
 * si -JAR
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class Auth0Controller {
    private static final Logger log = LoggerFactory.getLogger(Auth0Controller.class);

    static final String DYNAMIC_URI = "__dynamic__";
    static final String OAUTH_VUE_REDIRECT_URI = "oauth_vue_redirect_uri";

    private final Auth0Props properties;
    private final Auth0Service auth0Service;

    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpServletRequest request, HttpSession session) {
        String redirectUri = resolveBackendRedirectUri(properties.getRedirectUri(), request);
        String vueRedirectUri = resolveVueRedirectUri(properties.getVueRedirectUri(), request);
        session.setAttribute(OAUTH_VUE_REDIRECT_URI, vueRedirectUri);

        String authorizeUrl;
        try {
            authorizeUrl = auth0Service.createAuthorizeUrl(session, redirectUri);
        } catch (Auth0OAuthException exception) {
            log.warn("Auth0 authorize flow failed before redirecting to Auth0: {}", exception.getMessage());
            return redirectToVue(session, "error", "auth0_authorize_failed");
        }

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(authorizeUrl))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpSession session
    ) {
        if (error != null && !error.isBlank()) {
            return redirectToVue(session, "error", error);
        }

        if (code == null || code.isBlank() || state == null || state.isBlank()) {
            return redirectToVue(session, "error", "missing_oauth_callback_parameters");
        }

        Map<String, Object> tokens;
        try {
            tokens = auth0Service.exchangeCodeForTokens(
                    code,
                    state,
                    session
            );
        } catch (Auth0OAuthException exception) {
            log.warn("Auth0 callback flow failed before redirecting to Vue: {}", exception.getMessage());
            return redirectToVue(session, "error", "auth0_token_failed");
        }

        session.setAttribute("auth0_tokens", tokens);

        return redirectToVue(session, "success", null);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object tokens = session.getAttribute("auth0_tokens");

        if (tokens == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("authenticated", false)
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "authenticated", true
                )
        );
    }

    private String getStoredVueRedirectUri(HttpSession session) {
        Object value = session.getAttribute(OAUTH_VUE_REDIRECT_URI);

        if (!(value instanceof String vueRedirectUri) || vueRedirectUri.isBlank()) {
            throw Auth0OAuthException.badRequest("Missing Vue redirect URI");
        }

        return vueRedirectUri;
    }

    private ResponseEntity<Void> redirectToVue(HttpSession session, String loginState, String error) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(getStoredVueRedirectUri(session))
                .queryParam("login", loginState);

        if (error != null && !error.isBlank()) {
            builder.queryParam("error", error);
        }

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(builder.build().toUriString()))
                .build();
    }

    private String resolveBackendRedirectUri(String configuredUri, HttpServletRequest request) {
        String redirectUri = isDynamic(configuredUri)
                ? backendRedirectUriFromRequest(request)
                : configuredUri;

        return validateRedirectUri(redirectUri);
    }

    private String resolveVueRedirectUri(String configuredUri, HttpServletRequest request) {
        String redirectUri = isDynamic(configuredUri)
                ? vueRedirectUriFromRequestOrigin(request)
                : configuredUri;

        return validateRedirectUri(redirectUri);
    }

    private boolean isDynamic(String configuredUri) {
        return configuredUri == null || configuredUri.isBlank() || DYNAMIC_URI.equals(configuredUri);
    }

    private String backendRedirectUriFromRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();

        return UriComponentsBuilder
                .fromUriString(request.getRequestURL().toString())
                .replacePath(contextPath + "/auth/callback")
                .replaceQuery(null)
                .fragment(null)
                .build()
                .toUriString();
    }

    private String vueRedirectUriFromRequestOrigin(HttpServletRequest request) {
        URI origin = requestOrigin(request);

        return UriComponentsBuilder
                .fromUri(origin)
                .replacePath("/auth/callback")
                .replaceQuery(null)
                .fragment(null)
                .build()
                .toUriString();
    }

    private URI requestOrigin(HttpServletRequest request) {
        URI origin = originFromHeader(request.getHeader("Origin"));
        if (origin != null) {
            return origin;
        }

        origin = originFromHeader(request.getHeader("Referer"));
        if (origin != null) {
            return origin;
        }

        return URI.create(request.getRequestURL().toString());
    }

    private URI originFromHeader(String headerValue) {
        if (headerValue == null || headerValue.isBlank() || "null".equals(headerValue)) {
            return null;
        }

        URI uri = URI.create(headerValue);
        validateRedirectUri(uri.toString());

        return UriComponentsBuilder
                .newInstance()
                .scheme(uri.getScheme())
                .host(uri.getHost())
                .port(uri.getPort())
                .build()
                .toUri();
    }

    private String validateRedirectUri(String redirectUri) {
        URI uri = URI.create(redirectUri);
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

        return uri.toString();
    }
}
