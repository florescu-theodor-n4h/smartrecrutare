package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Reprezentarea publica a profilului analitic auditat. */
@Schema(description = "Profil analitic auditat al candidatului")
public record ProfilCandidatResponse(
        @Schema(description = "Identificatorul profilului") UUID id,
        @Schema(description = "Identificatorul candidatului", example = "12") Long candidatId,
        @Schema(description = "Numele candidatului", example = "Ion Popescu") String numeCandidat,
        @Schema(description = "Abilitatile normalizate") Set<String> abilitati,
        @Schema(description = "Locatiile preferate normalizate") Set<String> locatiiPreferate,
        @Schema(description = "Tipul de contract preferat") String tipContractPreferat,
        @Schema(description = "Cuvintele cheie normalizate") Set<String> cuvinteCheie,
        @Schema(description = "Data crearii", accessMode = Schema.AccessMode.READ_ONLY) Instant creatLa,
        @Schema(description = "Autorul crearii", accessMode = Schema.AccessMode.READ_ONLY) String creatDe,
        @Schema(description = "Data ultimei modificari", accessMode = Schema.AccessMode.READ_ONLY) Instant modificatLa,
        @Schema(description = "Autorul ultimei modificari", accessMode = Schema.AccessMode.READ_ONLY) String modificatDe,
        @Schema(description = "Versiunea optimista", accessMode = Schema.AccessMode.READ_ONLY) Long versiune
) {
}
