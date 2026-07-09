package com.samplus.smartrecrutare.auth.dev_auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(classes = DevSecConfigTest.TestApplication.class)
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "app.security.dev-jwt.basic-username=dev-user",
        "app.security.dev-jwt.basic-password=dev-pass"
})
class DevSecConfigTest {

    private static final String BASIC_CHALLENGE = "Basic realm=\"User Visible Realm\", charset=\"UTF-8\"";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DevAuthProperties properties;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void devAuthPropertiesBindBasicCredentials() {
        assertThat(properties.getBasicUsername()).isEqualTo("dev-user");
        assertThat(properties.getBasicPassword()).isEqualTo("dev-pass");
    }

    @Test
    void devAuthEndpointReturnsUtf8BasicChallengeWhenMissingCredentials() throws Exception {
        mockMvc.perform(get("/dev-auth/probe"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, BASIC_CHALLENGE));
    }

    @Test
    void devAuthEndpointRejectsInvalidBasicCredentialsWithSameChallenge() throws Exception {
        mockMvc.perform(get("/dev-auth/probe")
                        .with(httpBasic("dev-user", "wrong-pass")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, BASIC_CHALLENGE));
    }

    @Test
    void devAuthEndpointAcceptsConfiguredBasicCredentials() throws Exception {
        mockMvc.perform(get("/dev-auth/probe")
                        .with(httpBasic("dev-user", "dev-pass")))
                .andExpect(status().isOk())
                .andExpect(content().string("dev-user"));
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import(DevSecConfig.class)
    static class TestApplication {

        @Bean
        DevAuthProbeController devAuthProbeController() {
            return new DevAuthProbeController();
        }

        @Bean
        @Qualifier("jarPrivateKey")
        RSAPrivateKey jarPrivateKey(KeyPair devAuthKeyPair) {
            return (RSAPrivateKey) devAuthKeyPair.getPrivate();
        }

        @Bean
        @Qualifier("jarPublicKey")
        RSAPublicKey jarPublicKey(KeyPair devAuthKeyPair) {
            return (RSAPublicKey) devAuthKeyPair.getPublic();
        }

        @Bean
        KeyPair devAuthKeyPair() throws NoSuchAlgorithmException {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }
    }

    @RestController
    static class DevAuthProbeController {

        @GetMapping("/dev-auth/probe")
        String probe(Authentication authentication) {
            return authentication.getName();
        }
    }
}
