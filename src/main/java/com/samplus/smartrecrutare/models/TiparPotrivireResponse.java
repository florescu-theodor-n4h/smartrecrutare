package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** Reprezentarea administrativa a unui tipar auditat. */
@Schema(description = "Tipar de potrivire configurabil si auditat")
public record TiparPotrivireResponse(
        @Schema(description = "Identificatorul tiparului") UUID id,
        @Schema(description = "Numele unic") String nume,
        @Schema(description = "Descrierea administrativa") String descriere,
        @Schema(description = "Ponderea abilitatilor") int pondereAbilitati,
        @Schema(description = "Ponderea locatiei") int pondereLocatie,
        @Schema(description = "Ponderea contractului") int pondereContract,
        @Schema(description = "Ponderea cuvintelor cheie") int pondereCuvinteCheie,
        @Schema(description = "Pragul de notificare") int pragNotificare,
        @Schema(description = "Starea de activare") boolean activ,
        @Schema(description = "Data crearii", accessMode = Schema.AccessMode.READ_ONLY) Instant creatLa,
        @Schema(description = "Autorul crearii", accessMode = Schema.AccessMode.READ_ONLY) String creatDe,
        @Schema(description = "Data ultimei modificari", accessMode = Schema.AccessMode.READ_ONLY) Instant modificatLa,
        @Schema(description = "Autorul ultimei modificari", accessMode = Schema.AccessMode.READ_ONLY) String modificatDe,
        @Schema(description = "Versiunea optimista", accessMode = Schema.AccessMode.READ_ONLY) Long versiune
) {
}
