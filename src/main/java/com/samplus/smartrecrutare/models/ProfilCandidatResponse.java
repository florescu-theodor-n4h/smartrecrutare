package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Reprezentarea publica a profilului analitic auditat. */
@Schema(description = "Profil analitic auditat al candidatului")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilCandidatResponse {
    @Schema(description = "Identificatorul profilului") private UUID id;
    @Schema(description = "Identificatorul candidatului", example = "12") private Long candidatId;
    @Schema(description = "Numele candidatului", example = "Ion Popescu") private String numeCandidat;
    @Schema(description = "Abilitatile normalizate") private Set<String> abilitati;
    @Schema(description = "Locatiile preferate normalizate") private Set<String> locatiiPreferate;
    @Schema(description = "Tipul de contract preferat") private String tipContractPreferat;
    @Schema(description = "Cuvintele cheie normalizate") private Set<String> cuvinteCheie;
    @Schema(description = "Data crearii", accessMode = Schema.AccessMode.READ_ONLY) private Instant creatLa;
    @Schema(description = "Autorul crearii", accessMode = Schema.AccessMode.READ_ONLY) private String creatDe;
    @Schema(description = "Data ultimei modificari", accessMode = Schema.AccessMode.READ_ONLY) private Instant modificatLa;
    @Schema(description = "Autorul ultimei modificari", accessMode = Schema.AccessMode.READ_ONLY) private String modificatDe;
    @Schema(description = "Versiunea optimista", accessMode = Schema.AccessMode.READ_ONLY) private Long versiune;
}
