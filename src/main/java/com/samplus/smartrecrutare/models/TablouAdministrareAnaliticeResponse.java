package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

/** Indicatori agregati pentru pagina administratorului. */
@Schema(description = "Indicatorii principali ai modulului de analitice")
public record TablouAdministrareAnaliticeResponse(
        @Schema(description = "Numarul profilurilor analitice") long profiluriCandidati,
        @Schema(description = "Numarul joburilor active") long joburiActive,
        @Schema(description = "Numarul tiparelor active") long tipareActive,
        @Schema(description = "Numarul rezultatelor peste prag") long potriviriPestePrag,
        @Schema(description = "Numarul notificarilor necitite") long notificariNecitite,
        @Schema(description = "Indica existenta unei executii active") boolean executieActiva
) {
}
