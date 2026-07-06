package com.samplus.smartrecrutare.analytics.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Declara autentificarea Bearer folosita de API-urile analitice. */
@Configuration(proxyBeanMethods = false)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT cu rol sau scope pentru operatia solicitata"
)
public class ConfiguratieOpenApiAnalitice {
}
