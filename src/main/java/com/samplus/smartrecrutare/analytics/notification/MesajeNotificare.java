package com.samplus.smartrecrutare.analytics.notification;

/** Identificatori stabili pentru catalogul de traduceri al frontend-ului. */
public final class MesajeNotificare {
    public static final String POTRIVIRE_DISPONIBILA = "analytics.match.available";
    public static final String NUME_CANDIDAT = "candidateName";
    public static final String TITLU_JOB = "jobTitle";
    public static final String COMPANIE = "company";
    public static final String SCOR = "score";
    public static final String REZULTAT_ID = "matchId";

    private MesajeNotificare() {
        // Clasa utilitara nu poate fi instantiata.
    }
}
