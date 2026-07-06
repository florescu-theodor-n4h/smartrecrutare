package com.samplus.smartrecrutare.analytics.matching;

/** Port extensibil pentru algoritmi diferiti de potrivire. */
public interface StrategiePotrivire {
    ScorPotrivire calculeaza(
            DateProfilPotrivire profil,
            DateJobPotrivire job,
            DateTiparPotrivire tipar
    );
}
