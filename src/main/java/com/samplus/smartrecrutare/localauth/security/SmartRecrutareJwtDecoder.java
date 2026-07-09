package com.samplus.smartrecrutare.localauth.security;

import com.nimbusds.jwt.SignedJWT;
import com.samplus.smartrecrutare.localauth.config.LocalAuthProperties;
import com.samplus.smartrecrutare.localauth.service.LocalAuthTokenService;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.text.ParseException;

/** Alege validatorul JWT potrivit pentru tokenuri Auth0 sau LocalAuth. */
public class SmartRecrutareJwtDecoder implements JwtDecoder {
    private final String auth0Issuer;
    private final LocalAuthProperties localAuthProperties;
    private final LocalAuthTokenService localAuthTokenService;
    private volatile JwtDecoder auth0Decoder;

    public SmartRecrutareJwtDecoder(
            String auth0Issuer,
            LocalAuthProperties localAuthProperties,
            LocalAuthTokenService localAuthTokenService
    ) {
        this.auth0Issuer = auth0Issuer;
        this.localAuthProperties = localAuthProperties;
        this.localAuthTokenService = localAuthTokenService;
    }

    @Override
    @NullMarked
    public Jwt decode(String token) throws JwtException {
        if (isLocalToken(token)) {
            return localAuthTokenService.decode(token);
        }
        JwtDecoder decoder = auth0Decoder();
        if (decoder == null) {
            throw new JwtException("Nu exista decoder Auth0 configurat");
        }
        return decoder.decode(token);
    }

    private boolean isLocalToken(String token) {
        try {
            String issuer = SignedJWT.parse(token).getJWTClaimsSet().getIssuer();
            return localAuthProperties.getIssuer().equals(issuer);
        } catch (ParseException exception) {
            return false;
        }
    }

    private JwtDecoder auth0Decoder() {
        if (auth0Issuer == null || auth0Issuer.isBlank()) {
            return null;
        }
        JwtDecoder decoder = auth0Decoder;
        if (decoder == null) {
            synchronized (this) {
                decoder = auth0Decoder;
                if (decoder == null) {
                    decoder = NimbusJwtDecoder.withIssuerLocation(auth0Issuer).build();
                    auth0Decoder = decoder;
                }
            }
        }
        return decoder;
    }
}
