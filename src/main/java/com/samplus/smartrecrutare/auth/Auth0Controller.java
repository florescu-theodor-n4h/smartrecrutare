package com.samplus.smartrecrutare.auth;

import com.samplus.smartrecrutare.auth.config.Auth0Props;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
    private final Auth0Props properties;
    private final Auth0Service Auth0Service;

    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpSession session) {
        String authorizeUrl = Auth0Service.createAuthorizeUrl(session);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(authorizeUrl))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpSession session
    ) {
        Map<String, Object> tokens = Auth0Service.exchangeCodeForTokens(
                code,
                state,
                session
        );

        session.setAttribute("auth0_tokens", tokens);

        String vueUrl = UriComponentsBuilder
                .fromUriString(properties.getVueRedirectUri())
                .queryParam("login", "success")
                .build()
                .toUriString();

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(vueUrl))
                .build();
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
                        "authenticated", true,
                        "tokens", tokens
                )
        );
    }
}