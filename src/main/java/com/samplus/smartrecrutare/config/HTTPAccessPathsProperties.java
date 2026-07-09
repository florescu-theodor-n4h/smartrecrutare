package com.samplus.smartrecrutare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprietati rezervate pentru listele de rute HTTP folosite de securitatea aplicatiei.
 *
 * <p>Lista implicita pastreaza rutele publice ale SPA-ului si documentatiei API, iar valorile pot
 * fi suprascrise din configuratie prin {@code app.security.public-paths}.</p>
 */
@ConfigurationProperties(prefix = "app.security")
public class HTTPAccessPathsProperties {

    /**
     * Rutele publice permise fara JWT in lantul principal de securitate.
     */
    private List<String> publicPaths = new ArrayList<>(List.of(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    ));

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }
}
