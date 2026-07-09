package com.samplus.smartrecrutare.config;

import lombok.Getter;
import lombok.Setter;
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
    @Getter
    private final List<String> defaultPublicPaths = new ArrayList<>(List.of(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    ));
    // Rutele de sus sunt cele implicite, a nu se modifica acelea in .yaml
    private List<String> extraPublicPaths = new ArrayList<>();

    public void setExtraPublicPaths(List<String> extraPublicPaths) {
        this.extraPublicPaths.clear();
        this.extraPublicPaths.addAll(extraPublicPaths);
        //this.extraPublicPaths = extraPublicPaths;
        newHash = this.extraPublicPaths.hashCode();
    }

    private transient Set<String> mergedPaths = null;
    private transient String[] arr = {};
    private transient int oldHash=-1;
    private transient int newHash=-2;

    @NonNull
    public String[] getPublicPaths() {
        if(mergedPaths == null || oldHash != newHash) {
            this.arr = null;
            mergedPaths= new LinkedHashSet<>(defaultPublicPaths);
            mergedPaths.addAll(extraPublicPaths);
            oldHash =  extraPublicPaths.hashCode();
            newHash = oldHash;
            arr = mergedPaths.toArray(String[]::new);
        }
        return arr;
    }
}
