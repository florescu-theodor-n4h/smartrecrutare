package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

/** Componentele procentuale ale unui scor de potrivire. */
@Schema(description = "Componentele scorului, fiecare intre 0 si 100")
public record ScorDetaliatResponse(
        @Schema(description = "Scor pentru abilitati", example = "80") int abilitati,
        @Schema(description = "Scor pentru locatie", example = "100") int locatie,
        @Schema(description = "Scor pentru contract", example = "100") int contract,
        @Schema(description = "Scor pentru cuvinte cheie", example = "60") int cuvinteCheie
) {
}
