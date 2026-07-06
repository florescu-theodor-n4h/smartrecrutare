package com.samplus.smartrecrutare.analytics.matching;

import com.samplus.smartrecrutare.models.StarePotrivire;
import lombok.Value;

/** Rezultatul pur al strategiei, fara dependente JPA. */
@Value
public class ScorPotrivire {
    DateProfilPotrivire profil;
    DateJobPotrivire job;
    DateTiparPotrivire tipar;
    int total;
    int abilitati;
    int locatie;
    int contract;
    int cuvinteCheie;
    StarePotrivire stare;
}
