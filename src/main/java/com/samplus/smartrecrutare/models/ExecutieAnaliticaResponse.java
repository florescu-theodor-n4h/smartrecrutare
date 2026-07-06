package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** Starea observabila a unei executii asincrone. */
@Schema(description = "Executie asincrona pentru recalcularea potrivirilor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutieAnaliticaResponse {
    @Schema(description = "Identificatorul executiei") private UUID id;
    @Schema(description = "Starea curenta") private StareExecutieAnalitica stare;
    @Schema(description = "Numarul perechilor evaluate", example = "240") private long perechiEvaluate;
    @Schema(description = "Numarul potrivirilor peste prag", example = "18") private long potriviriPestePrag;
    @Schema(description = "Numarul notificarilor publicate", example = "7") private long notificariPublicate;
    @Schema(description = "Cod tehnic stabil pentru eroare, fara text localizat") private String codEroare;
    @Schema(description = "Momentul pornirii efective") private Instant pornitLa;
    @Schema(description = "Momentul finalizarii") private Instant finalizatLa;
    @Schema(description = "Momentul inregistrarii cererii") private Instant creatLa;
    @Schema(description = "Utilizatorul care a cerut executia") private String creatDe;
    @Schema(description = "Versiunea optimista") private Long versiune;
}
