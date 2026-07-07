package com.samplus.smartrecrutare.employer.dto;

import com.samplus.smartrecrutare.employer.domain.EmployerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cerere pentru crearea unui angajator")
public class EmployerCreateRequest {
    @NotBlank
    @Size(max = 150)
    private String nume;

    @NotBlank
    @Size(max = 200)
    private String denumireLegala;

    @NotBlank
    @Size(max = 40)
    private String codFiscal;

    @Email
    @Size(max = 180)
    private String emailContact;

    @Size(max = 40)
    private String telefonContact;

    @Size(max = 250)
    private String website;

    @Size(max = 150)
    private String locatie;

    @Size(max = 5000)
    private String descriere;

    @NotNull
    private EmployerStatus status;
}
