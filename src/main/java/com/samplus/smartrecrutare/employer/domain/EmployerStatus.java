package com.samplus.smartrecrutare.employer.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Starea operationala a angajatorului")
public enum EmployerStatus {
    ACTIV,
    INACTIV,
    SUSPENDAT,
    IN_VERIFICARE
}
