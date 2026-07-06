package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Componentele procentuale ale unui scor de potrivire. */
@Schema(description = "Componentele scorului, fiecare intre 0 si 100")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScorDetaliatResponse {
    @Schema(description = "Scor pentru abilitati", example = "80") private int abilitati;
    @Schema(description = "Scor pentru locatie", example = "100") private int locatie;
    @Schema(description = "Scor pentru contract", example = "100") private int contract;
    @Schema(description = "Scor pentru cuvinte cheie", example = "60") private int cuvinteCheie;
}
