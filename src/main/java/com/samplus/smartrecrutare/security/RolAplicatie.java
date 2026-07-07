package com.samplus.smartrecrutare.security;

/** Rolurile interne folosite de Spring Security. */
public enum RolAplicatie {
    ADMIN("admin", "ROLE_ADMIN"),
    MANAGER("manager", "ROLE_MANAGER"),
    AUDITOR("auditor", "ROLE_AUDITOR"),
    GOVERNMENTAL_USER("governmental-user", "ROLE_GOVERNMENTAL_USER"),
    USER("normal user", "ROLE_USER");

    private final String etichetaPublica;
    private final String autoritate;

    RolAplicatie(String etichetaPublica, String autoritate) {
        this.etichetaPublica = etichetaPublica;
        this.autoritate = autoritate;
    }

    public String getEtichetaPublica() {
        return etichetaPublica;
    }

    public String getAutoritate() {
        return autoritate;
    }
}
