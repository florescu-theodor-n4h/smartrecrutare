package com.samplus.smartrecrutare.analytics.service;

import com.samplus.smartrecrutare.analytics.exception.ConflictAnaliticException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/** Aplica aceeasi regula de concurenta optimista tuturor agregatelor. */
@Component
public class ValidatorVersiuneAnalitica {
    public void verifica(String resursa, Object id, Long actuala, Long solicitata) {
        if (!Objects.equals(actuala, solicitata)) {
            throw new ConflictAnaliticException(
                    resursa + " " + id + " are versiunea " + actuala
                            + ", dar cererea foloseste versiunea " + solicitata
            );
        }
    }
}
