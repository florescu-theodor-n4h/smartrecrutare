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
 * Controllerul principal pentru fluxul Auth0 al aplicatiei SPA.
 * Endpointurile nu expun tokenuri brute si transforma erorile OAuth in redirecturi controlate catre frontend.
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

    /**
     * Porneste autentificarea Auth0 si stocheaza in sesiune redirecturile validate pentru callback.
     * In caz de configuratie sau eroare Auth0, curata starea temporara si intoarce browserul catre SPA cu login=error.
     */
    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpServletRequest request, HttpSession session) {
        String vueRedirectUri = safeResolveVueRedirectUri(request);
        session.setAttribute(OAUTH_VUE_REDIRECT_URI, vueRedirectUri);
        log.info("Auth0 login requested session={} backendHost={} vueRedirectUri={}",
                safeSessionId(session),
                request.getServerName(),
                vueRedirectUri
        );

        String authorizeUrl;
        try {
            String redirectUri = resolveBackendRedirectUri(properties.getRedirectUri(), request);
            authorizeUrl = auth0Service.createAuthorizeUrl(session, redirectUri);
        } catch (Auth0OAuthException exception) {
            log.warn("Auth0 authorize flow failed session={} reason={}",
                    safeSessionId(session),
                    exception.getMessage()
            );
            auth0Service.clearTransientOauthSessionState(session);
            return redirectToVue(session, request, "error", "auth0_authorize_failed");
        } catch (RuntimeException exception) {
            log.error("Unexpected Auth0 authorize failure session={}", safeSessionId(session), exception);
            auth0Service.clearTransientOauthSessionState(session);
            return redirectToVue(session, request, "error", "auth0_authorize_failed");
        }

        log.info("Auth0 login redirect issued session={} authorizeHost={}", safeSessionId(session), properties.getDomain());
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(authorizeUrl))
                .build();
    }

    /**
     * Proceseaza callbackul Auth0, valideaza starea OAuth prin serviciu si redirectioneaza catre SPA.
     * Orice eroare OAuth sau callback incomplet este convertit intr-un redirect controlat cu login=error.
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request,
            HttpSession session
    ) {
        log.info("Auth0 callback received session={} hasCode={} hasState={} hasError={}",
                safeSessionId(session),
                code != null && !code.isBlank(),
                state != null && !state.isBlank(),
                error != null && !error.isBlank()
        );

        if (error != null && !error.isBlank()) {
            log.warn("Auth0 callback returned provider error session={} error={}", safeSessionId(session), error);
            auth0Service.clearTransientOauthSessionState(session);
            return redirectToVue(session, request, "error", error);
        }

        if (code == null || code.isBlank() || state == null || state.isBlank()) {
            log.warn("Auth0 callback missing required parameters session={} hasCode={} hasState={}",
                    safeSessionId(session),
                    code != null && !code.isBlank(),
                    state != null && !state.isBlank()
            );
            auth0Service.clearTransientOauthSessionState(session);
            return redirectToVue(session, request, "error", "missing_oauth_callback_parameters");
        }

        Map<String, Object> tokens;
        try {
            tokens = auth0Service.exchangeCodeForTokens(
                    code,
                    state,
                    session
            );
        } catch (Auth0OAuthException exception) {
            log.warn("Auth0 callback flow failed session={} reason={}",
                    safeSessionId(session),
                    exception.getMessage()
            );
            auth0Service.clearTransientOauthSessionState(session);
            return redirectToVue(session, request, "error", "auth0_token_failed");
        } catch (RuntimeException exception) {
            log.error("Unexpected Auth0 callback failure session={}", safeSessionId(session), exception);
            auth0Service.clearTransientOauthSessionState(session);
            return redirectToVue(session, request, "error", "auth0_token_failed");
        }

        session.setAttribute("auth0_tokens", tokens);

        log.info("Auth0 callback completed session={} authenticated=true", safeSessionId(session));
        return redirectToVue(session, request, "success", null);
    }

    /**
     * Raporteaza starea autentificarii Auth0 pentru sesiunea curenta fara a serializa tokenuri brute.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object tokens = session.getAttribute("auth0_tokens");

        if (tokens == null) {
            log.debug("Auth0 identity check session={} authenticated=false", safeSessionId(session));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("authenticated", false)
            );
        }

        log.debug("Auth0 identity check session={} authenticated=true", safeSessionId(session));
        return ResponseEntity.ok(
                Map.of(
                        "authenticated", true
                )
        );
    }

    /**
     * Returneaza redirectul SPA salvat in sesiune sau o valoare de rezerva calculata din configuratie/request.
     */
    private String getStoredVueRedirectUri(HttpSession session, HttpServletRequest request) {
        Object value = session.getAttribute(OAUTH_VUE_REDIRECT_URI);

        if (value instanceof String vueRedirectUri && !vueRedirectUri.isBlank()) {
            return vueRedirectUri;
        }

        String fallback = safeResolveVueRedirectUri(request);
        log.warn("Missing Vue redirect URI in session; falling back to {}", fallback);
        return fallback;
    }

    /**
     * Creeaza raspunsul de redirect catre SPA si ataseaza starea rezultatului autentificarii.
     */
    private ResponseEntity<Void> redirectToVue(HttpSession session, HttpServletRequest request, String loginState, String error) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(getStoredVueRedirectUri(session, request))
                .queryParam("login", loginState);

        if (error != null && !error.isBlank()) {
            builder.queryParam("error", error);
        }

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(builder.build().toUriString()))
                .build();
    }

    /**
     * Rezolva redirectul de backend trimis catre Auth0, folosind URL-ul requestului cand configuratia este dinamica.
     */
    private String resolveBackendRedirectUri(String configuredUri, HttpServletRequest request) {
        String redirectUri = isDynamic(configuredUri)
                ? backendRedirectUriFromRequest(request)
                : configuredUri;

        return validateRedirectUri(redirectUri);
    }

    /**
     * Rezolva redirectul SPA, folosind originea clientului cand configuratia este dinamica.
     */
    private String resolveVueRedirectUri(String configuredUri, HttpServletRequest request) {
        String redirectUri = isDynamic(configuredUri)
                ? vueRedirectUriFromRequestOrigin(request)
                : configuredUri;

        return validateRedirectUri(redirectUri);
    }

    /**
     * Verifica daca valoarea configurata cere calcul dinamic din request.
     */
    private boolean isDynamic(String configuredUri) {
        return configuredUri == null || configuredUri.isBlank() || DYNAMIC_URI.equals(configuredUri);
    }

    /**
     * Construieste callbackul backend pe aceeasi schema, host, port si context path ca requestul de login.
     */
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

    /**
     * Construieste callbackul SPA din originea sigura a requestului.
     */
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

    /**
     * Alege originea clientului din Origin, apoi Referer, apoi URL-ul requestului.
     */
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

    /**
     * Extrage doar originea dintr-un header HTTP, ignorand pathul si query stringul.
     */
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

    /**
     * Valideaza ca redirecturile OAuth sunt URL-uri absolute HTTP(S), fara user-info sau fragment.
     */
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

    /**
     * Calculeaza un redirect SPA robust; daca configuratia este invalida, foloseste originea requestului.
     */
    private String safeResolveVueRedirectUri(HttpServletRequest request) {
        try {
            return resolveVueRedirectUri(properties.getVueRedirectUri(), request);
        } catch (Auth0OAuthException | IllegalArgumentException exception) {
            String fallback = fallbackVueRedirectUri(request);
            log.warn("Invalid Vue redirect configuration; using request fallback {}", fallback);
            return fallback;
        }
    }

    /**
     * Creeaza callback SPA de rezerva strict din schema, host si portul requestului curent.
     */
    private String fallbackVueRedirectUri(HttpServletRequest request) {
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(request.getScheme()) && port == 80)
                || ("https".equalsIgnoreCase(request.getScheme()) && port == 443);

        UriComponentsBuilder builder = UriComponentsBuilder
                .newInstance()
                .scheme(request.getScheme())
                .host(request.getServerName())
                .path("/auth/callback");

        if (!defaultPort) {
            builder.port(port);
        }

        return builder.build().toUriString();
    }

    /**
     * Logheaza un identificator de sesiune derivat, fara a expune ID-ul brut.
     */
    private String safeSessionId(HttpSession session) {
        return session.getId() == null ? "unknown" : Integer.toHexString(session.getId().hashCode());
    }
}
