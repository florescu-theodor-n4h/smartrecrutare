package com.samplus.smartrecrutare.analytics.matching;

import java.util.Set;

/** Copie imutabila a profilului necesar algoritmului. */
public record DateProfilPotrivire(
        Long candidatId,
        String numeCandidat,
        String emailCandidat,
        Set<String> abilitati,
        Set<String> locatiiPreferate,
        String tipContractPreferat,
        Set<String> cuvinteCheie
) {
}
