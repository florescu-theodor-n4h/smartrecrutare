package com.samplus.smartrecrutare.auth.dev_auth;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Proprietati pentru autentificarea Basic folosita exclusiv de endpoint-urile {@code /dev-auth/**}.
 *
 * <p>Valorile implicite pastreaza ergonomia locala, iar validarea opreste pornirea profilului
 * {@code dev} cand credentialele sunt golite accidental.</p>
 */
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

    public String getBasicUsername() {
        return basicUsername;
    }

    public void setBasicUsername(String basicUsername) {
        this.basicUsername = basicUsername;
    }

    public String getBasicPassword() {
        return basicPassword;
    }

    public void setBasicPassword(String basicPassword) {
        this.basicPassword = basicPassword;
    }
}
