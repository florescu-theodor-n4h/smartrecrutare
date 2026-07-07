package com.samplus.smartrecrutare.analytics.security;

import com.samplus.smartrecrutare.security.RoluriAplicatie;

/** Expresii unice pentru autorizarea metodelor modulului. */
public final class RoluriAnalitice {
    public static final String ADMIN = RoluriAplicatie.ADMIN;
    public static final String ADMIN_SAU_RECRUITER = RoluriAplicatie.ADMIN_OR_MANAGER;
    public static final String CITIRE_ADMINISTRATIVA = RoluriAplicatie.ADMIN_READ;
    public static final String AUTENTIFICAT = RoluriAplicatie.AUTHENTICATED;

    private RoluriAnalitice() {
        // Clasa utilitara nu poate fi instantiata.
    }
}
