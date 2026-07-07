package com.samplus.smartrecrutare.localauth.service;

import com.samplus.smartrecrutare.localauth.config.LocalAuthProperties;
import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthDisabledException;
import com.samplus.smartrecrutare.security.RolAplicatie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

/** Emite si valideaza tokenuri JWT semnate local pentru LocalAuth. */
@Service
public class LocalAuthTokenService {
    private static final String CLAIM_AUTH_PROVIDER = "auth_provider";
    private static final String LOCAL_PROVIDER = "local";
    private static final String CLAIM_ROLES = "roles";

    private final LocalAuthProperties properties;

    public LocalAuthTokenService(LocalAuthProperties properties) {
        this.properties = properties;
    }

    public TokenData createToken(LocalUser user) {
        ensureEnabled();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getAccessTokenMinutes() * 60);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(CLAIM_AUTH_PROVIDER, LOCAL_PROVIDER)
                .claim(CLAIM_ROLES, user.getRoles().stream().map(RolAplicatie::getAutoritate).toList())
                .claim("local_user_id", user.getId())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = encoder().encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenData(token, expiresAt);
    }

    public Jwt decode(String token) {
        ensureEnabled();
        return decoder().decode(token);
    }

    public boolean isEnabled() {
        return properties.isEnabled() && properties.hasSigningSecret();
    }

    private JwtEncoder encoder() {
        return new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableSecret<>(secretKey()));
    }

    private JwtDecoder decoder() {
        return NimbusJwtDecoder.withSecretKey(secretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private SecretKeySpec secretKey() {
        return new SecretKeySpec(properties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
    }

    private void ensureEnabled() {
        if (!isEnabled()) {
            throw new LocalAuthDisabledException();
        }
    }

    public record TokenData(String token, Instant expiresAt) {
    }
}
