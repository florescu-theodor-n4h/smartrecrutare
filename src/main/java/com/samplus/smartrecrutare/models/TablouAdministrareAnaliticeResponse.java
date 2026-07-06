package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Indicatori agregati pentru pagina administratorului. */
@Schema(description = "Indicatorii principali ai modulului de analitice")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TablouAdministrareAnaliticeResponse {
    @Schema(description = "Numarul profilurilor analitice") private long profiluriCandidati;
    @Schema(description = "Numarul joburilor active") private long joburiActive;
    @Schema(description = "Numarul tiparelor active") private long tipareActive;
    @Schema(description = "Numarul rezultatelor peste prag") private long potriviriPestePrag;
    @Schema(description = "Numarul notificarilor necitite") private long notificariNecitite;
    @Schema(description = "Indica existenta unei executii active") private boolean executieActiva;
}
