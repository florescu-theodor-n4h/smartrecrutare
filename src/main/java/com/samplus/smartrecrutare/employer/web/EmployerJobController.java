package com.samplus.smartrecrutare.employer.web;

import com.samplus.smartrecrutare.ServiciuJoburi;
import com.samplus.smartrecrutare.job.dto.JobPublishRequest;
import com.samplus.smartrecrutare.job.dto.JobResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expune fluxul de publicare a joburilor in contextul angajatorului.
 */
@RestController
@Validated
@RequestMapping("/api/employers/{employerId}/jobs")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Joburi angajator", description = "Publicarea joburilor de catre angajatori")
public class EmployerJobController {

    private final ServiciuJoburi serviciuJoburi;

    public EmployerJobController(ServiciuJoburi serviciuJoburi) {
        this.serviciuJoburi = serviciuJoburi;
    }

    @Operation(
            summary = "Publica un job pentru angajator",
            description = "Managerul trebuie sa administreze angajatorul din URL. ID-ul angajatorului nu se transmite in corpul cererii."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Job publicat",
                    content = @Content(schema = @Schema(implementation = JobResponse.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide"),
            @ApiResponse(responseCode = "403", description = "Managerul nu administreaza angajatorul"),
            @ApiResponse(responseCode = "404", description = "Angajator inexistent")
    })
    @PostMapping
    @PreAuthorize("@localAuthorizationService.canManageEmployer(#employerId)")
    public ResponseEntity<JobResponse> publica(
            @PathVariable Long employerId,
            @Valid @RequestBody JobPublishRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviciuJoburi.crearePentruAngajator(employerId, request));
    }
}
