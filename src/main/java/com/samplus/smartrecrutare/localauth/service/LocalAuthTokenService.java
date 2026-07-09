package com.samplus.smartrecrutare.localauth.service;

import com.samplus.smartrecrutare.localauth.config.LocalAuthProperties;
import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthDisabledException;
import com.samplus.smartrecrutare.security.RolAplicatie;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

/** Emite si valideaza tokenuri JWT semnate local pentru LocalAuth. */
@Service
public class LocalAuthTokenService {
    private static final String CLAIM_AUTH_PROVIDER = "auth_provider";
    private static final String LOCAL_PROVIDER = "local";
    private static final String CLAIM_ROLES = "roles";

    private final LocalAuthProperties properties;
    private final JwtEncoder localAuthJwtEncoder;
    private final JwtDecoder localAuthJwtDecoder;

    public LocalAuthTokenService(
            LocalAuthProperties properties,
            @Qualifier("localAuthJwtEncoder") JwtEncoder localAuthJwtEncoder,
            @Qualifier("localAuthJwtDecoder") JwtDecoder localAuthJwtDecoder
    ) {
        this.properties = properties;
        this.localAuthJwtEncoder = localAuthJwtEncoder;
        this.localAuthJwtDecoder = localAuthJwtDecoder;
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

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .keyId(properties.getKeyId())
                .type("JWT")
                .build();
        String token = localAuthJwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenData(token, expiresAt);
    }

    public Jwt decode(String token) {
        ensureEnabled();
        return localAuthJwtDecoder.decode(token);
    }

    public boolean isEnabled() {
        return properties.isEnabled() && properties.hasSigningSecret();
    }

    private void ensureEnabled() {
        if (!isEnabled()) {
            throw new LocalAuthDisabledException();
        }
    }

    public record TokenData(String token, Instant expiresAt) {
    }
}
