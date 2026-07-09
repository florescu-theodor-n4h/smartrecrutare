package com.samplus.smartrecrutare.localauth.service;

import com.nimbusds.jwt.SignedJWT;
import com.samplus.smartrecrutare.localauth.config.LocalAuthJwtConfig;
import com.samplus.smartrecrutare.localauth.config.LocalAuthProperties;
import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthDisabledException;
import com.samplus.smartrecrutare.security.RolAplicatie;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalAuthTokenServiceTest {
    private final LocalAuthJwtConfig jwtConfig = new LocalAuthJwtConfig();

    @Test
    void createsHs256TokenWithLocalIssuerKeyIdAndRoles() throws Exception {
        LocalAuthProperties properties = properties();
        LocalAuthTokenService service = service(properties);
        LocalUser user = user();

        LocalAuthTokenService.TokenData token = service.createToken(user);
        SignedJWT signedJWT = SignedJWT.parse(token.token());
        Jwt decoded = service.decode(token.token());

        assertThat(signedJWT.getHeader().getAlgorithm().getName()).isEqualTo("HS256");
        assertThat(signedJWT.getHeader().getKeyID()).isEqualTo("auth-local-test-sym-v1");
        assertThat(decoded.getClaimAsString("iss")).isEqualTo("smartrecrutare-local-test");
        assertThat(decoded.getSubject()).isEqualTo("local-admin");
        assertThat(decoded.getClaimAsStringList("roles")).containsExactly("ROLE_ADMIN");
        assertThat(decoded.<Long>getClaim("local_user_id")).isEqualTo(42L);
    }

    @Test
    void rejectsLocalTokenWhenVerifiedWithDifferentSymmetricKey() {
        LocalAuthProperties properties = properties();
        LocalAuthTokenService service = service(properties);
        LocalAuthTokenService.TokenData token = service.createToken(user());

        LocalAuthProperties wrongProperties = properties();
        wrongProperties.setSecret(Base64.getEncoder().encodeToString(
                "wrong-local-auth-secret-32-bytes!".getBytes(StandardCharsets.UTF_8)
        ));
        JwtDecoder wrongDecoder = jwtConfig.localAuthJwtDecoder(secretKey(wrongProperties));

        assertThatThrownBy(() -> wrongDecoder.decode(token.token()))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void refusesToCreateTokensWhenLocalAuthIsDisabled() {
        LocalAuthProperties properties = properties();
        properties.setEnabled(false);
        LocalAuthTokenService service = service(properties);

        assertThat(service.isEnabled()).isFalse();
        assertThatThrownBy(() -> service.createToken(user()))
                .isInstanceOf(LocalAuthDisabledException.class);
    }

    private LocalAuthTokenService service(LocalAuthProperties properties) {
        SecretKey key = secretKey(properties);
        return new LocalAuthTokenService(
                properties,
                jwtConfig.localAuthJwtEncoder(properties, key),
                jwtConfig.localAuthJwtDecoder(key)
        );
    }

    private SecretKey secretKey(LocalAuthProperties properties) {
        return jwtConfig.localAuthSecretKey(properties, new DefaultResourceLoader());
    }

    private LocalAuthProperties properties() {
        LocalAuthProperties properties = new LocalAuthProperties();
        properties.setEnabled(true);
        properties.setIssuer("smartrecrutare-local-test");
        properties.setKeyId("auth-local-test-sym-v1");
        properties.setSecretLocation("classpath:local-secrets/auth-local-symmetric.key");
        properties.setAccessTokenMinutes(30);
        return properties;
    }

    private LocalUser user() {
        LocalUser user = LocalUser.creare(
                "local-admin",
                "local-admin@example.test",
                "$2a$hash",
                Set.of(RolAplicatie.ADMIN)
        );
        ReflectionTestUtils.setField(user, "id", 42L);
        return user;
    }
}
