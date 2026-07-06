package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** Rezultatul persistat pentru o pereche candidat-job. */
@Schema(description = "Rezultat auditat pentru potrivirea candidatului cu jobul")
public record RezultatPotrivireResponse(
        @Schema(description = "Identificatorul rezultatului") UUID id,
        @Schema(description = "Identificatorul candidatului") Long candidatId,
        @Schema(description = "Numele candidatului") String numeCandidat,
        @Schema(description = "Identificatorul jobului") Long jobId,
        @Schema(description = "Titlul jobului") String titluJob,
        @Schema(description = "Compania") String companie,
        @Schema(description = "Identificatorul tiparului") UUID tiparId,
        @Schema(description = "Numele tiparului") String numeTipar,
        @Schema(description = "Scorul ponderat final", example = "82") int scorTotal,
        @Schema(description = "Componentele scorului") ScorDetaliatResponse detalii,
        @Schema(description = "Clasificarea fata de prag") StarePotrivire stare,
        @Schema(description = "Momentul ultimei evaluari") Instant evaluatLa,
        @Schema(description = "Versiunea optimista") Long versiune
) {
}
