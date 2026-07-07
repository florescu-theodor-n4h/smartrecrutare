package com.samplus.smartrecrutare.localauth.web;

import com.samplus.smartrecrutare.localauth.dto.LocalLoginRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalLoginResponse;
import com.samplus.smartrecrutare.localauth.service.LocalAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Expune autentificarea locala si verificarea identitatii curente. */
@RestController
@RequestMapping("/auth/local")
@Tag(name = "LocalAuth", description = "Autentificare locala pentru utilizatori din baza de date")
public class LocalAuthController {
    private final LocalAuthService localAuthService;

    public LocalAuthController(LocalAuthService localAuthService) {
        this.localAuthService = localAuthService;
    }

    @Operation(summary = "Autentifica un utilizator local")
    @PostMapping("/login")
    public ResponseEntity<LocalLoginResponse> login(@Valid @RequestBody LocalLoginRequest request) {
        return ResponseEntity.ok(localAuthService.login(request));
    }

    @Operation(summary = "Returneaza identitatea curenta")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "authenticated", authentication != null && authentication.isAuthenticated(),
                "name", authentication == null ? "" : authentication.getName(),
                "authorities", authentication == null ? java.util.List.of() : authentication.getAuthorities()
        ));
    }
}
