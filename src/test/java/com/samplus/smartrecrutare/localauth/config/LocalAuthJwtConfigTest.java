package com.samplus.smartrecrutare.localauth.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalAuthJwtConfigTest {
    private final LocalAuthJwtConfig config = new LocalAuthJwtConfig();

    @Test
    void loadsOpenSslBase64SecretFromClasspathResource() {
        LocalAuthProperties properties = properties();
        properties.setSecret("");
        properties.setSecretLocation("classpath:local-secrets/auth-local-symmetric.key");

        SecretKey key = config.localAuthSecretKey(properties, new DefaultResourceLoader());

        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(key.getEncoded()).hasSize(64);
    }

    @Test
    void acceptsInlineBase64SecretAsFallbackForOperationalOverrides() {
        LocalAuthProperties properties = properties();
        properties.setSecret(Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes()));
        properties.setSecretLocation("");

        SecretKey key = config.localAuthSecretKey(properties, new DefaultResourceLoader());

        assertThat(key.getEncoded()).hasSize(32);
    }

    @Test
    void rejectsSymmetricSecretsShorterThanHs256Minimum() {
        LocalAuthProperties properties = properties();
        properties.setSecret("short");
        properties.setSecretLocation("");

        assertThatThrownBy(() -> config.localAuthSecretKey(properties, new DefaultResourceLoader()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cel putin 32 de bytes");
    }

    @Test
    void disabledLocalAuthDoesNotRequireSecretFileAtStartup() {
        LocalAuthProperties properties = properties();
        properties.setEnabled(false);
        properties.setSecret("");
        properties.setSecretLocation("classpath:local-secrets/missing-local-key.key");

        SecretKey key = config.localAuthSecretKey(properties, new DefaultResourceLoader());

        assertThat(key.getEncoded()).hasSizeGreaterThanOrEqualTo(32);
    }

    private LocalAuthProperties properties() {
        LocalAuthProperties properties = new LocalAuthProperties();
        properties.setEnabled(true);
        properties.setIssuer("smartrecrutare-local-test");
        properties.setKeyId("auth-local-test-sym-v1");
        return properties;
    }
}
