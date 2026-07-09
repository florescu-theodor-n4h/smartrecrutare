package com.samplus.smartrecrutare.localauth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Proprietati pentru autentificarea locala bazata pe utilizatori din baza de date. */
@Getter
@Setter
@ConfigurationProperties(prefix = "local-auth")
public class LocalAuthProperties {
    private boolean enabled;
    private String issuer = "smartrecrutare-local";
    private String keyId = "auth-local-sym-v1";
    private String secretLocation = "classpath:local-secrets/auth-local-symmetric.key";
    private String secret;
    private long accessTokenMinutes = 60;

    public boolean hasSigningSecret() {
        return (secret != null && !secret.isBlank()) || (secretLocation != null && !secretLocation.isBlank());
    }
}
