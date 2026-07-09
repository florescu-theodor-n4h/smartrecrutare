package com.samplus.smartrecrutare.auth.config;

import com.samplus.smartrecrutare.config.SecurityConfig;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Este activat din {@link SecurityConfig}
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "auth0")
public class Auth0Props {
    private String domain;
    private String clientId;
    private String clientSecret;
    private String audience;
    private String redirectUri;
    private String vueRedirectUri;
    private Jar jar = new Jar();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Jar {
        private String keyId;
        private String privateKeyLocation;
        private String publicKeyLocation;
    }
}
