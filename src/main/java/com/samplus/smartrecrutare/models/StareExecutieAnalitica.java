package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

/** Starile posibile ale unei executii analitice asincrone. */
@Schema(description = "Starea unei executii analitice asincrone")
public enum StareExecutieAnalitica {
    IN_ASTEPTARE,
    IN_EXECUTIE,
    FINALIZATA,
    ESUATA
}
