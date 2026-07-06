package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Inlocuire versionata a profilului analitic. */
@Schema(description = "Cerere versionata pentru inlocuirea profilului analitic")
public record ActualizareProfilCandidatRequest(
        @Valid @NotNull ProfilCandidatRequest profil,
        @Schema(description = "Versiunea curenta pentru control concurent", example = "0")
        @NotNull @PositiveOrZero Long versiune
) {
}
