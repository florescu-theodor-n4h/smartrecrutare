package com.samplus.smartrecrutare.employer.dto;

import com.samplus.smartrecrutare.employer.domain.EmployerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Raspuns complet pentru un angajator auditat")
public class EmployerResponse {
    private Long id;
    private String nume;
    private String denumireLegala;
    private String codFiscal;
    private String emailContact;
    private String telefonContact;
    private String website;
    private String locatie;
    private String descriere;
    private EmployerStatus status;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private Instant creatLa;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private String creatDe;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private Instant modificatLa;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private String modificatDe;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private Long versiune;
}
