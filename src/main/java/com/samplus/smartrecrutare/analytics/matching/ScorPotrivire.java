package com.samplus.smartrecrutare.analytics.matching;

import com.samplus.smartrecrutare.models.StarePotrivire;

/** Rezultatul pur al strategiei, fara dependente JPA. */
public record ScorPotrivire(
        DateProfilPotrivire profil,
        DateJobPotrivire job,
        DateTiparPotrivire tipar,
        int total,
        int abilitati,
        int locatie,
        int contract,
        int cuvinteCheie,
        StarePotrivire stare
) {
}
