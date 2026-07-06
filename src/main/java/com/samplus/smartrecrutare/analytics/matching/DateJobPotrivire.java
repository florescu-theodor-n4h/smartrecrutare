package com.samplus.smartrecrutare.analytics.matching;

/** Copie imutabila a jobului necesar algoritmului. */
public record DateJobPotrivire(
        Long jobId,
        String titlu,
        String descriere,
        String companie,
        String locatie,
        String tipContract
) {
}
