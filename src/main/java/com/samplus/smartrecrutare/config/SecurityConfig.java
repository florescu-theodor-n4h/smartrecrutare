package com.samplus.smartrecrutare.config;

import com.samplus.smartrecrutare.auth.Auth0OAuthException;
import com.samplus.smartrecrutare.auth.config.Auth0Props;
import com.samplus.smartrecrutare.auth.dev_auth.DevAuthProperties;
import com.samplus.smartrecrutare.localauth.config.LocalAuthProperties;
import com.samplus.smartrecrutare.localauth.security.SmartRecrutareJwtDecoder;
import com.samplus.smartrecrutare.security.RbacJwtAuthenticationConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Configuratia principala de securitate pentru API, SPA si resursele OAuth2/JWT.
 *
 * <p>Endpoint-urile speciale {@code /dev-auth/**} sunt tratate de lantul dedicat din
 * {@code DevSecConfig}; aici raman refuzate daca lantul dedicat nu este disponibil.</p>
 */
@Configuration
@EnableConfigurationProperties({Auth0Props.class, LocalAuthProperties.class, HTTPAccessPathsProperties.class})
@EnableWebSecurity
@EnableMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true
)
public class SecurityConfig {
    private void disableCsrf(@NonNull CsrfConfigurer<HttpSecurity> conf) {
        conf.disable();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            HTTPAccessPathsProperties accessPathsProperties,
            @Qualifier("multiDecoder") JwtDecoder multiDecoder
    ) throws Exception {
        final String[] publicPaths = accessPathsProperties.getPublicPaths();

        return http
                .csrf(this::disableCsrf)
                .cors(Customizer.withDefaults())
                .sessionManagement(new Customizer<SessionManagementConfigurer<HttpSecurity>>() {
                    @Override
                    public void customize(SessionManagementConfigurer<HttpSecurity> sesiune) {
                        sesiune.sessionCreationPolicy(STATELESS);
                    }
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicPaths).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/dev-auth/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs/active").permitAll()
                        .requestMatchers("/auth/login", "/auth/callback", "/auth/me").permitAll()
                        .requestMatchers("/auth/local/login", "/auth/local/register").permitAll()
                        .requestMatchers("/auth/local/me").authenticated()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/bot/**").authenticated()
                        .requestMatchers("/bots/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.decoder(multiDecoder)
                                .jwtAuthenticationConverter(rbacJwtAuthenticationConverter())
                ))
                .build();
    }

    /*@Bean
    @Qualifier("multiDecoder")
    JwtDecoder multiDecoder(
    ) {
        return new MultiDecoder();
    } TODO*/

    @Component("multiDecoder")
    //@RequiredArgsConstructor
    @NullMarked
    protected static final class MultiDecoder implements JwtDecoder {
        /* @Qualifier("devDecoder") */           private final JwtDecoder devDecoder;
        /*@Qualifier("localAuthJwtDecoder")*/    private final JwtDecoder localAuthJwtDecoder;
        /*@Qualifier("auth0Decoder")*/           private final JwtDecoder auth0Decoder;
        private final                            LocalAuthProperties localAuthProperties;
        public MultiDecoder(@Qualifier("devDecoder")            JwtDecoder devDecoder,
                            @Qualifier("localAuthJwtDecoder")   JwtDecoder localAuthJwtDecoder,
                            @Qualifier("auth0Decoder")          JwtDecoder auth0Decoder,
                            LocalAuthProperties localAuthProperties) {
            this.devDecoder = devDecoder;
            this.localAuthJwtDecoder = localAuthJwtDecoder;
            this.auth0Decoder = auth0Decoder;
            this.localAuthProperties = localAuthProperties;
        }

        @Override
        public Jwt decode(String token) throws JwtException {
            String issuer = JwtHelper.issuer(token);
            if (DevAuthProperties.ISSUER_DEV.equals(issuer)) {
                return devDecoder.decode(token);
            }
            if (localAuthProperties.getIssuer().equals(issuer)) {
                return localAuthJwtDecoder.decode(token);
            }
            return auth0Decoder.decode(token);
        }
    }

    @Bean
    RbacJwtAuthenticationConverter rbacJwtAuthenticationConverter() {
        return new RbacJwtAuthenticationConverter();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @Primary
    JwtDecoder smartRecrutareJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String auth0Issuer,
            LocalAuthProperties localAuthProperties,
            @Qualifier("localAuthJwtDecoder") JwtDecoder localAuthJwtDecoder
    ) {
        return new SmartRecrutareJwtDecoder(auth0Issuer, localAuthProperties, localAuthJwtDecoder);
    }

    @Bean
    @Qualifier("auth0Decoder")
    JwtDecoder auth0Decoder(@Qualifier("smartRecrutareJwtDecoder") JwtDecoder smartRecrutareJwtDecoder) {
        return smartRecrutareJwtDecoder;
    }

    @Bean
    @Qualifier("secureRestClient")
    RestClient secureRestClient(Auth0Props props, RestClient.Builder builder) {
        if (!props.getDomain().endsWith(".auth0.com")) {
            throw new IllegalStateException("Unexpected Auth0 domain: " + props.getDomain());
        }
        HttpClient httpClient = HttpClient.newBuilder()
                //.connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_2)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        //requestFactory.setReadTimeout(Duration.ofSeconds(10));
        requestFactory.enableCompression(false);

        return builder
                .requestFactory(requestFactory)
                .baseUrl("https://" + props.getDomain())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, response) -> {
                            String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                            throw Auth0OAuthException.upstream(
                                    request.getURI().getPath(),
                                    response.getStatusCode().value(),
                                    responseBody
                            );
                        }
                )
                .build();
    }
}
