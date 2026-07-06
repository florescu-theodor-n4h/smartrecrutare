package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Inlocuire versionata a unui tipar de potrivire. */
@Schema(description = "Cerere versionata pentru inlocuirea unui tipar")
public record ActualizareTiparPotrivireRequest(
        @Valid @NotNull CreareTiparPotrivireRequest tipar,
        @Schema(description = "Versiunea curenta pentru control concurent", example = "0")
        @NotNull @PositiveOrZero Long versiune
) {
}
