package com.samplus.smartrecrutare.security;

/** Expresii centralizate pentru regulile RBAC ale aplicatiei. */
public final class RoluriAplicatie {
    public static final String ADMIN = "hasAuthority('ROLE_ADMIN')";
    public static final String ADMIN_OR_MANAGER = "hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER')";
    public static final String BUSINESS_READ =
            "hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_AUDITOR','ROLE_GOVERNMENTAL_USER')";
    public static final String ADMIN_READ =
            "hasAnyAuthority('ROLE_ADMIN','ROLE_AUDITOR','ROLE_GOVERNMENTAL_USER')";
    public static final String AUTHENTICATED = "isAuthenticated()";

    private RoluriAplicatie() {
        // Clasa utilitara nu poate fi instantiata.
    }
}
