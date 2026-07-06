package com.samplus.smartrecrutare.analytics.security;

/** Expresii unice pentru autorizarea metodelor modulului. */
public final class RoluriAnalitice {
    public static final String ADMIN = "hasAnyAuthority('ROLE_ADMIN','SCOPE_admin')";
    public static final String ADMIN_SAU_RECRUITER =
            "hasAnyAuthority('ROLE_ADMIN','SCOPE_admin','ROLE_RECRUITER','SCOPE_recruiter')";
    public static final String AUTENTIFICAT = "isAuthenticated()";

    private RoluriAnalitice() {
        // Clasa utilitara nu poate fi instantiata.
    }
}
