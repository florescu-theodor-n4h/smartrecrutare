package com.samplus.smartrecrutare.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.samplus.smartrecrutare.auth.PemLoader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

@Configuration
public class JarJwtConf {
    /**
     * Genereaza o cheie tranzitorie doar pentru testele automate.
     *
     * <p>Profilul de productie continua sa solicite explicit cheile PEM
     * configurate, astfel incat testele nu introduc secrete in repository.</p>
     */
    @Bean("testJarKeyPair")
    @Profile("test")
    public KeyPair testJarKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    @Bean
    @Qualifier("jarPrivateKey")
    public RSAPrivateKey jarPrivateKey(
            Auth0Props properties,
            ResourceLoader resourceLoader,
            PemLoader pemLoader,
            @Qualifier("testJarKeyPair") ObjectProvider<KeyPair> testJarKeyPair
    ) throws Exception {
        Resource resource = resourceLoader.getResource(
                properties.getJar().getPrivateKeyLocation()
        );
        if (!resource.exists() && testJarKeyPair.getIfAvailable() != null) {
            return (RSAPrivateKey) testJarKeyPair.getObject().getPrivate();
        }

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
            PemLoader pemLoader,
            @Qualifier("testJarKeyPair") ObjectProvider<KeyPair> testJarKeyPair
    ) throws Exception {
        Resource resource = resourceLoader.getResource(
                properties.getJar().getPublicKeyLocation()
        );
        if (!resource.exists() && testJarKeyPair.getIfAvailable() != null) {
            return (RSAPublicKey) testJarKeyPair.getObject().getPublic();
        }

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
