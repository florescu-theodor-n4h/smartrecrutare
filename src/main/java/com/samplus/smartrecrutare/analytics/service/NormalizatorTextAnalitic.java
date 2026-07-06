package com.samplus.smartrecrutare.analytics.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Centralizeaza normalizarea datelor configurabile. */
@Component
public class NormalizatorTextAnalitic {
    public Set<String> multime(Set<String> valori) {
        if (valori == null) {
            return Set.of();
        }
        return valori.stream()
                .filter(StringUtils::hasText)
                .map(this::text)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public String textOptional(String valoare) {
        return StringUtils.hasText(valoare) ? text(valoare) : null;
    }

    public String text(String valoare) {
        String faraAccente = Normalizer.normalize(valoare, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return faraAccente.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}
