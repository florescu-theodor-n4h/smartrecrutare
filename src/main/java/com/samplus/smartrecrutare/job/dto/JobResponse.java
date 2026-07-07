package com.samplus.smartrecrutare.job.dto;

import com.samplus.smartrecrutare.employer.dto.EmployerSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Raspuns pentru un job publicat")
public class JobResponse {
    private Long id;
    private String titlu;
    private String descriere;
    private String companie;
    private String locatie;
    private String salariu;
    private String tipContract;
    private boolean activ;
    private EmployerSummaryResponse employer;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private Instant creatLa;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private String creatDe;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private Instant modificatLa;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private String modificatDe;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) private Long versiune;
}
