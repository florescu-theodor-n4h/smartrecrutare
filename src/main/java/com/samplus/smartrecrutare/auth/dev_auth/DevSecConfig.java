package com.samplus.smartrecrutare.auth.dev_auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@Profile("dev")
public class DevSecConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain devAuthSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/dev-auth/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic
                        .realmName("User Visible Realm")
                )
                .build();
    }

    @Bean
    public UserDetailsService devAuthUsers(
            @Value("${app.security.dev-jwt.basic-username:dev}") String username,
            @Value("${app.security.dev-jwt.basic-password:dev}") String password
    ) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password("{noop}" + password)
                        .roles("DEV_AUTH")
                        .build()
        );
    }
}