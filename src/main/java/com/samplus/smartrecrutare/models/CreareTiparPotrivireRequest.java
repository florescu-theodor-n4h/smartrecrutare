package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Configuratia ponderilor folosite de algoritmul de potrivire. */
@Schema(description = "Cerere pentru un tipar nou de potrivire; ponderile trebuie sa insumeze 100")
public record CreareTiparPotrivireRequest(
        @Schema(description = "Numele unic al tiparului", example = "Potrivire tehnica standard")
        @NotBlank @Size(max = 150) String nume,

        @Schema(description = "Descriere administrativa", example = "Tipar folosit pentru roluri tehnice")
        @Size(max = 500) String descriere,

        @Schema(description = "Ponderea abilitatilor", example = "55")
        @Min(0) @Max(100) int pondereAbilitati,

        @Schema(description = "Ponderea locatiei", example = "15")
        @Min(0) @Max(100) int pondereLocatie,

        @Schema(description = "Ponderea tipului de contract", example = "10")
        @Min(0) @Max(100) int pondereContract,

        @Schema(description = "Ponderea cuvintelor cheie", example = "20")
        @Min(0) @Max(100) int pondereCuvinteCheie,

        @Schema(description = "Pragul minim pentru notificare", example = "70")
        @Min(0) @Max(100) int pragNotificare,

        @Schema(description = "Indica daca tiparul este folosit de executarile noi", example = "true")
        @NotNull Boolean activ
) {
}
