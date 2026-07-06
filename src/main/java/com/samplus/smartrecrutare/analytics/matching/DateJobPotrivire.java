package com.samplus.smartrecrutare.analytics.matching;

import lombok.Value;

/** Copie imutabila a jobului necesar algoritmului. */
@Value
public class DateJobPotrivire {
    Long jobId;
    String titlu;
    String descriere;
    String companie;
    String locatie;
    String tipContract;
}
