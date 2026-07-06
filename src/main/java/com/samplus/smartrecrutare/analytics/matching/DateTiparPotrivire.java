package com.samplus.smartrecrutare.analytics.matching;

import java.util.UUID;

/** Copie imutabila a ponderilor unui tipar activ. */
public record DateTiparPotrivire(
        UUID tiparId,
        String nume,
        int pondereAbilitati,
        int pondereLocatie,
        int pondereContract,
        int pondereCuvinteCheie,
        int pragNotificare
) {
}
