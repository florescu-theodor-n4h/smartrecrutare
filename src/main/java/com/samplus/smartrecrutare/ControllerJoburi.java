package com.samplus.smartrecrutare;

import com.samplus.smartrecrutare.job.dto.JobCreateRequest;
import com.samplus.smartrecrutare.job.dto.JobResponse;
import com.samplus.smartrecrutare.job.dto.JobUpdateRequest;
import com.samplus.smartrecrutare.security.RoluriAplicatie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Joburi", description = "CRUD pentru posturile de munca disponibile")
public class ControllerJoburi {

    private final ServiciuJoburi serviciuJoburi;

    public ControllerJoburi(ServiciuJoburi serviciuJoburi) {
        this.serviciuJoburi = serviciuJoburi;
    }

    @Operation(summary = "Obtine toate joburile")
    @ApiResponse(responseCode = "200", description = "Lista joburi",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobResponse.class))))
    @GetMapping
    @PreAuthorize(RoluriAplicatie.BUSINESS_READ)
    public ResponseEntity<Collection<JobResponse>> getJobs() {
        return ResponseEntity.ok(serviciuJoburi.getToateJoburileDto());
    }

    @Operation(summary = "Obtine un job dupa ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job gasit",
                    content = @Content(schema = @Schema(implementation = JobResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job inexistent")
    })
    @GetMapping("/{id}")
    @PreAuthorize(RoluriAplicatie.BUSINESS_READ)
    public ResponseEntity<JobResponse> getJob(
            @Parameter(description = "ID-ul jobului", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(serviciuJoburi.gasireDto(id));
    }

    @Operation(
            summary = "Creeaza un job nou",
            description = "Creeaza un job si il leaga de angajatorul existent indicat prin employerId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Job creat",
                    content = @Content(schema = @Schema(implementation = JobResponse.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide"),
            @ApiResponse(responseCode = "404", description = "Angajator inexistent")
    })
    @PostMapping
    @PreAuthorize("@localAuthorizationService.canManageEmployer(#request.employerId)")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviciuJoburi.creareDinRequest(request));
    }

    @Operation(
            summary = "Inlocuieste un job existent",
            description = "Inlocuieste campurile publicabile ale jobului si poate schimba angajatorul asociat."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job actualizat",
                    content = @Content(schema = @Schema(implementation = JobResponse.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide"),
            @ApiResponse(responseCode = "404", description = "Job sau angajator inexistent")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@localAuthorizationService.canManageJob(#id)")
    public ResponseEntity<JobResponse> updateJob(
            @Parameter(description = "ID-ul jobului de actualizat", required = true)
            @PathVariable Long id,
            @Valid @RequestBody JobUpdateRequest request
    ) {
        return ResponseEntity.ok(serviciuJoburi.inlocuire(id, request));
    }

    @Operation(summary = "Sterge un job dupa ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rezultatul operatiei true/false"),
            @ApiResponse(responseCode = "400", description = "ID invalid")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(RoluriAplicatie.ADMIN)
    public ResponseEntity<Boolean> deleteJob(
            @Parameter(description = "ID-ul jobului de sters", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(serviciuJoburi.stergere(id));
    }

    @Operation(summary = "Obtine toate joburile active")
    @ApiResponse(responseCode = "200", description = "Lista joburi active",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobResponse.class))))
    @GetMapping("/active")
    public ResponseEntity<List<JobResponse>> getJoburiActive() {
        return ResponseEntity.ok(serviciuJoburi.getJoburiActiveDto());
    }

    @Operation(summary = "Cauta joburi dupa titlu")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rezultate cautare",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Parametrul titlu lipseste sau este gol")
    })
    @GetMapping("/cauta")
    public ResponseEntity<List<JobResponse>> cautaDupaTitlu(@RequestParam String titlu) {
        return ResponseEntity.ok(serviciuJoburi.cautareDupaTitluDto(titlu));
    }
}
