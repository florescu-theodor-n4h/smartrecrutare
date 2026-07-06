package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

/** Starile unei notificari disponibile in interfata utilizatorului. */
@Schema(description = "Starea notificarii")
public enum StareNotificare {
    NOUA,
    CITITA
}
