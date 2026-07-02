package com.samplus.smartrecrutare.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.samplus.smartrecrutare.auth.PemLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JarJwtConf {
    @Bean
    @Qualifier("jarPrivateKey")
    public RSAPrivateKey jarPrivateKey(
            Auth0Props properties,
            ResourceLoader resourceLoader,
            PemLoader pemLoader
    ) throws Exception {
        Resource resource = resourceLoader.getResource(
                properties.getJar().getPrivateKeyLocation()
        );

        String pem;
        try (InputStream inputStream = resource.getInputStream()) {
            pem = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        return pemLoader.readPrivateKey(pem);
    }

    @Bean
    @Qualifier("jarPublicKey")
    public RSAPublicKey jarPublicKey(
            Auth0Props properties,
            ResourceLoader resourceLoader,
            PemLoader pemLoader
    ) throws Exception {
        Resource resource = resourceLoader.getResource(
                properties.getJar().getPublicKeyLocation()
        );

        String pem;
        try (InputStream inputStream = resource.getInputStream()) {
            pem = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        return pemLoader.readPublicKey(pem);
    }

    @Bean
    //@Primary
    @Qualifier("jarJwtEncoder")
    public JwtEncoder jarJwtEncoder(
            Auth0Props properties,
            @Qualifier("jarPublicKey") RSAPublicKey jarPublicKey,
            @Qualifier("jarPrivateKey") RSAPrivateKey jarPrivateKey
    ) {
        RSAKey rsaKey = new RSAKey.Builder(jarPublicKey)
                .privateKey(jarPrivateKey)
                .keyID(properties.getJar().getKeyId())
                .build();

        return new NimbusJwtEncoder(
                new ImmutableJWKSet<>(new JWKSet(rsaKey))
        );
    }
}
