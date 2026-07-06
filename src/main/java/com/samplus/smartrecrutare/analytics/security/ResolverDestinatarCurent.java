package com.samplus.smartrecrutare.analytics.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/** Alege adresa de email din JWT, cu revenire la numele autentificarii. */
@Component
public class ResolverDestinatarCurent {
    public String rezolva(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String email = jwt.getClaimAsString("email");
            if (StringUtils.hasText(email)) {
                return email.trim().toLowerCase(Locale.ROOT);
            }
        }
        return authentication.getName().trim().toLowerCase(Locale.ROOT);
    }
}
