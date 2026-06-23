package com.samplus.smartrecrutare;

import org.springframework.data.jpa.repository.JpaRepository;
import io.swagger.v3.oas.annotations.media.Schema;

/***/
@Schema(description = "Depozitul de candidati stocati in baza de date")
public interface DepozitCandidati extends JpaRepository<Candidat, Long> {
}
