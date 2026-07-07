package com.samplus.smartrecrutare.employer.web;

import com.samplus.smartrecrutare.employer.dto.EmployerCreateRequest;
import com.samplus.smartrecrutare.employer.dto.EmployerResponse;
import com.samplus.smartrecrutare.employer.dto.EmployerUpdateRequest;
import com.samplus.smartrecrutare.employer.service.EmployerService;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.security.RoluriAplicatie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Validated
@RequestMapping("/api/employers")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Angajatori", description = "Administrarea angajatorilor care publica joburi")
public class EmployerController {

    private final EmployerService employerService;

    public EmployerController(EmployerService employerService) {
        this.employerService = employerService;
    }

    @Operation(summary = "Creeaza un angajator")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Angajator creat",
                    content = @Content(schema = @Schema(implementation = EmployerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide"),
            @ApiResponse(responseCode = "409", description = "Cod fiscal duplicat")
    })
    @PostMapping
    @PreAuthorize(RoluriAplicatie.ADMIN_OR_MANAGER)
    public ResponseEntity<EmployerResponse> create(@Valid @RequestBody EmployerCreateRequest request) {
        EmployerResponse response = employerService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Listeaza angajatorii")
    @GetMapping
    @PreAuthorize(RoluriAplicatie.BUSINESS_READ)
    public ResponseEntity<PaginaModel<EmployerResponse>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(employerService.list(PageRequest.of(page, size)));
    }

    @Operation(summary = "Obtine un angajator dupa id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Angajator gasit",
                    content = @Content(schema = @Schema(implementation = EmployerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Angajator inexistent")
    })
    @GetMapping("/{id}")
    @PreAuthorize(RoluriAplicatie.BUSINESS_READ)
    public ResponseEntity<EmployerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employerService.getById(id));
    }

    @Operation(summary = "Inlocuieste datele unui angajator")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Angajator actualizat",
                    content = @Content(schema = @Schema(implementation = EmployerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide"),
            @ApiResponse(responseCode = "404", description = "Angajator inexistent"),
            @ApiResponse(responseCode = "409", description = "Cod fiscal duplicat")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@localAuthorizationService.canManageEmployer(#id)")
    public ResponseEntity<EmployerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployerUpdateRequest request
    ) {
        return ResponseEntity.ok(employerService.update(id, request));
    }

    @Operation(summary = "Sterge un angajator fara joburi asociate")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Angajator sters"),
            @ApiResponse(responseCode = "404", description = "Angajator inexistent"),
            @ApiResponse(responseCode = "409", description = "Angajator folosit de joburi")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(RoluriAplicatie.ADMIN)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
