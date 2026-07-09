package com.samplus.smartrecrutare.auth.dev_auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Configuratie de securitate pentru endpoint-urile locale de dezvoltare.
 *
 * <p>Lantul are prioritate peste resource server pentru {@code /dev-auth/**}, ca endpoint-ul de token
 * local sa raspunda cu provocare HTTP Basic, nu cu Bearer. Credentialele sunt citite din
 * {@link DevAuthProperties}, ca sa ramana configurabile fara valori ascunse in cod.</p>
 */
@Configuration
// @Profile("dev")
@EnableConfigurationProperties(DevAuthProperties.class)
public class DevSecConfig {

    /**
     * Protejeaza toate endpoint-urile {@code /dev-auth/**} cu HTTP Basic dedicat mediului de dezvoltare.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain devAuthSecurityFilterChain(
            HttpSecurity http,
            AuthenticationManager devAuthAuthenticationManager,
            AuthenticationEntryPoint devBasicEntryPoint
    ) throws Exception {
        return http
                .securityMatcher("/dev-auth/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationManager(devAuthAuthenticationManager)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(devBasicEntryPoint)
                )
                .build();
    }

    /**
     * Trimite provocarea Basic exact in formatul acceptat de browsere si clienti HTTP moderni.
     */
    @Bean
    public AuthenticationEntryPoint devBasicEntryPoint() {
        return (request, response, authException) -> {
            response.setHeader(
                    HttpHeaders.WWW_AUTHENTICATE,
                    "Basic realm=\"User Visible Realm\", charset=\"UTF-8\""
            );
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        };
    }

    /**
     * Construieste utilizatorul local folosit numai pentru obtinerea token-urilor de dezvoltare.
     */
    @Bean
    public UserDetailsService devAuthUsers(DevAuthProperties properties) {
        return new InMemoryUserDetailsManager(
                User.withUsername(properties.getBasicUsername())
                        // NoOpPasswordEncoder este izolat la profilul dev; pastram parola exact cum vine din configuratie.
                        .password(properties.getBasicPassword())
                        .roles("DEV_AUTH")
                        .build()
        );
    }

    /**
     * Izoleaza autentificarea Basic pentru lantul {@code /dev-auth/**}, fara sa afecteze JWT-ul aplicatiei.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationManager devAuthAuthenticationManager(UserDetailsService devAuthUsers) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(devAuthUsers);
        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());

        return new ProviderManager(provider);
    }

    @Bean
    @Qualifier("devEncoder")
    JwtEncoder devEncoder(
            @Qualifier("jarPrivateKey") RSAPrivateKey jarPrivateKey,
            @Qualifier("jarPublicKey") RSAPublicKey jarPublicKey
    ) {
        RSAKey rsaKey = new RSAKey.Builder(jarPublicKey)
                .privateKey(jarPrivateKey)
                .keyID("dev-key")
                .build();

        JWKSource<SecurityContext> jwkSource =
                new ImmutableJWKSet<>(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    @Qualifier("devDecoder")
    JwtDecoder devDecoder(
            @Qualifier("jarPublicKey") RSAPublicKey jarPublicKey
    ) {
        return NimbusJwtDecoder
                .withPublicKey(jarPublicKey)
                .build();
    }
}
