package com.samplus.smartrecrutare.analytics.matching;

import lombok.Value;

import java.util.UUID;

/** Copie imutabila a ponderilor unui tipar activ. */
@Value
public class DateTiparPotrivire {
    UUID tiparId;
    String nume;
    int pondereAbilitati;
    int pondereLocatie;
    int pondereContract;
    int pondereCuvinteCheie;
    int pragNotificare;
}
