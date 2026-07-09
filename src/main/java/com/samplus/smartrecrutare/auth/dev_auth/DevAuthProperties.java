package com.samplus.smartrecrutare.auth.dev_auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;

/**
 * Proprietati pentru autentificarea Basic folosita exclusiv de endpoint-urile {@code /dev-auth/**}.
 *
 * <p>Valorile implicite pastreaza ergonomia locala, iar validarea opreste pornirea profilului
 * {@code dev} cand credentialele sunt golite accidental.</p>
 */
@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "app.security.dev-jwt")
public class DevAuthProperties {

    /**
     * Utilizatorul HTTP Basic pentru endpoint-urile de dezvoltare.
     */
    @NotBlank
    private String basicUsername = "dev";

    /**
     * Parola HTTP Basic pentru endpoint-urile de dezvoltare.
     */
    @NotBlank
    private String basicPassword = "dev";

    // proprietati interne
    public static final String ISSUER_DEV="local-dev";
    public static final String SUBJECT_DEV="swagger-dev-user";
    public static final List<String> ROLES_DEV = Arrays.asList("admin", "recruiter");
}
