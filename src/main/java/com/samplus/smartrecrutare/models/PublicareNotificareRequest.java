package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Cerere administrativa pentru publicarea unui mesaj localizabil. */
@Schema(description = "Cerere de publicare fara text intr-o limba hardcodata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicareNotificareRequest {
    @Schema(description = "Destinatarul tehnic", example = "ion.popescu@email.com")
    @NotBlank
    @Size(max = 320)
    private String destinatar;

    @Schema(description = "Cheia din catalogul frontend", example = "analytics.profile.updated")
    @NotBlank
    @Size(max = 160)
    @Pattern(regexp = "[a-z0-9][a-z0-9._-]+")
    private String mesajId;

    @Schema(description = "Substituenti serializabili pentru mesaj", example = "{\"candidateName\":\"Ion Popescu\"}")
    @Size(max = 30)
    private Map<@NotBlank @Size(max = 80) String, @NotBlank @Size(max = 500) String> substituenti;
}
