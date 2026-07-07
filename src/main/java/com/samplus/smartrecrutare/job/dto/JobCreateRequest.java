package com.samplus.smartrecrutare.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cerere pentru crearea unui job legat de un angajator")
public class JobCreateRequest {
    @NotBlank
    @Size(max = 180)
    private String titlu;

    @Size(max = 10000)
    private String descriere;

    @NotNull
    private Long employerId;

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
