package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/** Datele configurabile folosite la potrivirea unui candidat. */
@Schema(description = "Date pentru crearea sau inlocuirea profilului analitic")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilCandidatRequest {
    @ArraySchema(schema = @Schema(example = "java"), minItems = 1, maxItems = 50)
    @Size(min = 1, max = 50)
    private Set<@NotBlank @Size(max = 80) String> abilitati;

    @ArraySchema(schema = @Schema(example = "bucuresti"), maxItems = 20)
    @Size(max = 20)
    private Set<@NotBlank @Size(max = 120) String> locatiiPreferate;

    @Schema(description = "Tipul de contract preferat", example = "Full-time", maxLength = 80)
    @Size(max = 80)
    private String tipContractPreferat;

    @ArraySchema(schema = @Schema(example = "spring"), maxItems = 50)
    @Size(max = 50)
    private Set<@NotBlank @Size(max = 80) String> cuvinteCheie;
}
