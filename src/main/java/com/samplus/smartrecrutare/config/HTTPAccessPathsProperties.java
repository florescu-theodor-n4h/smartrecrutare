package com.samplus.smartrecrutare.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private final List<String> defaultPublicPaths = new ArrayList<>(List.of(
            "/",
            "/index.html",
            "/favicon.ico",

            "/assets/**",
            "/css/**",
            "/js/**",
            "/img/**",
            "/images/**",
            "/static/**",

            "/manifest.webmanifest",
            "/robots.txt",

            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    ));

    /**
     * Rute publice adaugate din configuratie peste lista implicita.
     */
    private List<String> extraPublicPaths = new ArrayList<>();

    public List<String> getDefaultPublicPaths() {
        return List.copyOf(defaultPublicPaths);
    }

    public List<String> getExtraPublicPaths() {
        return List.copyOf(extraPublicPaths);
    }

    public void setExtraPublicPaths(List<String> extraPublicPaths) {
        this.extraPublicPaths.clear();
        if (extraPublicPaths != null) {
            this.extraPublicPaths.addAll(extraPublicPaths);
        }
    }

    /**
     * Returneaza rutele publice finale, fara duplicate si in ordine stabila.
     */
    @NonNull
    public String[] getPublicPaths() {
        Set<String> mergedPaths = new LinkedHashSet<>(defaultPublicPaths);
        mergedPaths.addAll(extraPublicPaths);
        return mergedPaths.toArray(String[]::new);
    }
}
