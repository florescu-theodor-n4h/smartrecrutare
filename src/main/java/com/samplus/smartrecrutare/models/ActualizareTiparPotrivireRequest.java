package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Inlocuire versionata a unui tipar de potrivire. */
@Schema(description = "Cerere versionata pentru inlocuirea unui tipar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizareTiparPotrivireRequest {
    @Valid
    @NotNull
    private CreareTiparPotrivireRequest tipar;

    @Schema(description = "Versiunea curenta pentru control concurent", example = "0")
    @NotNull
    @PositiveOrZero
    private Long versiune;
}
