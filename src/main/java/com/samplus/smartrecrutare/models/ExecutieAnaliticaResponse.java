package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** Starea observabila a unei executii asincrone. */
@Schema(description = "Executie asincrona pentru recalcularea potrivirilor")
public record ExecutieAnaliticaResponse(
        @Schema(description = "Identificatorul executiei") UUID id,
        @Schema(description = "Starea curenta") StareExecutieAnalitica stare,
        @Schema(description = "Numarul perechilor evaluate", example = "240") long perechiEvaluate,
        @Schema(description = "Numarul potrivirilor peste prag", example = "18") long potriviriPestePrag,
        @Schema(description = "Numarul notificarilor publicate", example = "7") long notificariPublicate,
        @Schema(description = "Cod tehnic stabil pentru eroare, fara text localizat") String codEroare,
        @Schema(description = "Momentul pornirii efective") Instant pornitLa,
        @Schema(description = "Momentul finalizarii") Instant finalizatLa,
        @Schema(description = "Momentul inregistrarii cererii") Instant creatLa,
        @Schema(description = "Utilizatorul care a cerut executia") String creatDe,
        @Schema(description = "Versiunea optimista") Long versiune
) {
}
