package com.samplus.smartrecrutare.config;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.BadJwtException;

import java.text.ParseException;
import java.util.Map;

/**
 * Citeste parti nevalidate dintr-un JWT numai pentru alegerea decoderului corect.
 *
 * <p>Acest helper nu autentifica tokenul. Dupa rutare, decoderul ales verifica semnatura,
 * algoritmul si claim-urile standard.</p>
 */
final class JwtHelper {
    private JwtHelper() {
    }

    static String issuer(String token) {
        Object issuer = headersAndClaims(token).getClaims().get("iss");
        return issuer == null ? "" : String.valueOf(issuer);
    }

    static TokenParts headersAndClaims(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            return new TokenParts(jwt.getHeader().toJSONObject(), jwt.getJWTClaimsSet().getClaims());
        } catch (ParseException exception) {
            throw new BadJwtException("JWT-ul nu poate fi parsat pentru rutarea decoderului", exception);
        }
    }

    record TokenParts(Map<String, Object> headers, Map<String, Object> claims) {
        Map<String, Object> getClaims() {
            return claims;
        }
    }
}
