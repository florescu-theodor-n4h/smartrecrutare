package com.samplus.smartrecrutare.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(Auth0Props.class)
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .build();
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
