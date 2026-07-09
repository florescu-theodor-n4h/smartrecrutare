package com.samplus.smartrecrutare.localauth.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Configureaza lantul JWT local bazat pe cheie simetrica separata.
 *
 * <p>Cheia LocalAuth este izolata de cheile Auth0/JAR si este folosita doar pentru tokenurile emise
 * local. Fisierul poate fi generat cu {@code openssl rand -base64 64} si trebuie sa contina cel
 * putin 32 de bytes dupa decodarea Base64.</p>
 */
@Configuration
public class LocalAuthJwtConfig {
    private static final int MINIMUM_HS256_KEY_BYTES = 32;
    private static final byte[] DISABLED_LOCAL_AUTH_PLACEHOLDER =
            "disabled-local-auth-placeholder-secret".getBytes(StandardCharsets.UTF_8);

    @Bean
    @Qualifier("localAuthSecretKey")
    public SecretKey localAuthSecretKey(LocalAuthProperties properties, ResourceLoader resourceLoader) {
        byte[] keyBytes = loadKeyBytes(properties, resourceLoader);
        if (keyBytes.length < MINIMUM_HS256_KEY_BYTES) {
            throw new IllegalStateException("Cheia JWT locala trebuie sa aiba cel putin 32 de bytes");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    @Qualifier("localAuthJwtEncoder")
    public JwtEncoder localAuthJwtEncoder(
            LocalAuthProperties properties,
            @Qualifier("localAuthSecretKey") SecretKey localAuthSecretKey
    ) {
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(localAuthSecretKey)
                .keyID(properties.getKeyId())
                .algorithm(JWSAlgorithm.HS256)
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    @Qualifier("localAuthJwtDecoder")
    public JwtDecoder localAuthJwtDecoder(@Qualifier("localAuthSecretKey") SecretKey localAuthSecretKey) {
        return NimbusJwtDecoder.withSecretKey(localAuthSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private byte[] loadKeyBytes(LocalAuthProperties properties, ResourceLoader resourceLoader) {
        String inlineSecret = properties.getSecret();
        if (inlineSecret != null && !inlineSecret.isBlank()) {
            return decodeSecret(inlineSecret);
        }

        String location = properties.getSecretLocation();
        if (location == null || location.isBlank()) {
            if (!properties.isEnabled()) {
                return DISABLED_LOCAL_AUTH_PLACEHOLDER;
            }
            throw new IllegalStateException("Nu exista local-auth.secret-location configurat");
        }

        Resource resource = resourceLoader.getResource(location);
        try (InputStream inputStream = resource.getInputStream()) {
            return decodeSecret(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            if (!properties.isEnabled()) {
                return DISABLED_LOCAL_AUTH_PLACEHOLDER;
            }
            throw new IllegalStateException("Cheia JWT locala nu poate fi citita din " + location, exception);
        }
    }

    private byte[] decodeSecret(String secret) {
        String trimmed = secret.trim();
        try {
            return Base64.getMimeDecoder().decode(trimmed);
        } catch (IllegalArgumentException ignored) {
            return trimmed.getBytes(StandardCharsets.UTF_8);
        }
    }
}
