package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** Rezultatul persistat pentru o pereche candidat-job. */
@Schema(description = "Rezultat auditat pentru potrivirea candidatului cu jobul")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RezultatPotrivireResponse {
    @Schema(description = "Identificatorul rezultatului") private UUID id;
    @Schema(description = "Identificatorul candidatului") private Long candidatId;
    @Schema(description = "Numele candidatului") private String numeCandidat;
    @Schema(description = "Identificatorul jobului") private Long jobId;
    @Schema(description = "Titlul jobului") private String titluJob;
    @Schema(description = "Compania") private String companie;
    @Schema(description = "Identificatorul tiparului") private UUID tiparId;
    @Schema(description = "Numele tiparului") private String numeTipar;
    @Schema(description = "Scorul ponderat final", example = "82") private int scorTotal;
    @Schema(description = "Componentele scorului") private ScorDetaliatResponse detalii;
    @Schema(description = "Clasificarea fata de prag") private StarePotrivire stare;
    @Schema(description = "Momentul ultimei evaluari") private Instant evaluatLa;
    @Schema(description = "Versiunea optimista") private Long versiune;
}
