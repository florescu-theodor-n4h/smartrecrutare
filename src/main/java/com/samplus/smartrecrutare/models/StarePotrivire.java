package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

/** Clasificarea rezultatului fata de pragul tiparului. */
@Schema(description = "Starea rezultatului de potrivire")
public enum StarePotrivire {
    PESTE_PRAG,
    SUB_PRAG
}
