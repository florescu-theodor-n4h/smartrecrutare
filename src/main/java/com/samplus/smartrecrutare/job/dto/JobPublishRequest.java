package com.samplus.smartrecrutare.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cererea prin care un angajator publica un job.
 *
 * <p>Identitatea angajatorului este preluata exclusiv din URL, pentru a nu
 * permite ca un manager sa trimita in corpul cererii ID-ul altei companii.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cerere pentru publicarea unui job de catre un angajator")
public class JobPublishRequest {
    @NotBlank
    @Size(max = 180)
    private String titlu;

    @Size(max = 10000)
    private String descriere;

    @Size(max = 180)
    private String companie;

    @Size(max = 180)
    private String locatie;

    @Size(max = 120)
    private String salariu;

    @NotBlank
    @Size(max = 80)
    private String tipContract;

    private Boolean activ = Boolean.TRUE;
}
