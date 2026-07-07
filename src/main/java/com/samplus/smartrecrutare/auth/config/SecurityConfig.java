package com.samplus.smartrecrutare.auth.config;

import com.samplus.smartrecrutare.localauth.config.LocalAuthProperties;
import com.samplus.smartrecrutare.localauth.security.SmartRecrutareJwtDecoder;
import com.samplus.smartrecrutare.localauth.service.LocalAuthTokenService;
import com.samplus.smartrecrutare.security.RbacJwtAuthenticationConverter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.http.HttpClient;
import java.util.Arrays;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableConfigurationProperties({Auth0Props.class, LocalAuthProperties.class})
@EnableWebSecurity
@EnableMethodSecurity
        (
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
        config.setAllowedHeaders(Arrays.asList("*")) ;
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs/active").permitAll()
                        .requestMatchers("/auth/login", "/auth/callback", "/auth/me").permitAll()
                        .requestMatchers("/auth/local/login").permitAll()
                        .requestMatchers("/auth/local/me").authenticated()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/bot/**").authenticated()
                        .requestMatchers("/bots/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(rbacJwtAuthenticationConverter())
                ))
                .build();
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
    JwtDecoder smartRecrutareJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String auth0Issuer,
            LocalAuthProperties localAuthProperties,
            LocalAuthTokenService localAuthTokenService
    ) {
        return new SmartRecrutareJwtDecoder(auth0Issuer, localAuthProperties, localAuthTokenService);
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
                            throw new IllegalStateException(
                                    "Cererea a esuat, cu codul de eroare: " + response.getStatusCode()
                            );
                        }
                )
                .build();
    }

}
