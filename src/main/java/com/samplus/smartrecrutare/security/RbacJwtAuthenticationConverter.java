package com.samplus.smartrecrutare.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Converteste rolurile din JWT in autoritati Spring Security uniforme. */
public class RbacJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_AUTHORITIES = "authorities";
    private static final String CLAIM_AUTH0_ROLES = "https://smartrecrutare/roles";

    private final JwtAuthenticationConverter delegate;
    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

    public RbacJwtAuthenticationConverter() {
        this.delegate = new JwtAuthenticationConverter();
        this.delegate.setJwtGrantedAuthoritiesConverter(this::convertAuthorities);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        return delegate.convert(source);
    }

    private Collection<GrantedAuthority> convertAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(scopeConverter.convert(jwt));
        addClaimAuthorities(jwt, CLAIM_ROLES, authorities);
        addClaimAuthorities(jwt, CLAIM_AUTHORITIES, authorities);
        addClaimAuthorities(jwt, CLAIM_AUTH0_ROLES, authorities);
        return authorities;
    }

    private void addClaimAuthorities(Jwt jwt, String claimName, Set<GrantedAuthority> authorities) {
        Object claim = jwt.getClaims().get(claimName);
        if (claim instanceof Collection<?> values) {
            values.stream()
                    .map(String::valueOf)
                    .map(this::normalizeAuthority)
                    .filter(value -> !value.isBlank())
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        } else if (claim instanceof String value) {
            for (String role : value.split("[, ]+")) {
                String authority = normalizeAuthority(role);
                if (!authority.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(authority));
                }
            }
        }
    }

    private String normalizeAuthority(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return "";
        }
        String normalized = rawRole.trim();
        if (normalized.startsWith("ROLE_") || normalized.startsWith("SCOPE_")) {
            return normalized;
        }
        return "ROLE_" + normalized
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }
}
