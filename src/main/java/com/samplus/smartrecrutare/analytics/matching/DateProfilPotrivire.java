package com.samplus.smartrecrutare.analytics.matching;

import lombok.Value;

import java.util.Set;

/** Copie imutabila a profilului necesar algoritmului. */
@Value
public class DateProfilPotrivire {
    Long candidatId;
    String numeCandidat;
    String emailCandidat;
    Set<String> abilitati;
    Set<String> locatiiPreferate;
    String tipContractPreferat;
    Set<String> cuvinteCheie;
}
