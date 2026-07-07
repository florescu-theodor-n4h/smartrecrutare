package com.samplus.smartrecrutare.employer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Rezumat angajator expus in raspunsurile pentru joburi")
public class EmployerSummaryResponse {
    private Long id;
    private String nume;
    private String denumireLegala;
}
