package com.samplus.smartrecrutare.localauth.web;

import com.samplus.smartrecrutare.localauth.dto.LocalUserCreateRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalUserPasswordRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalUserResponse;
import com.samplus.smartrecrutare.localauth.dto.LocalUserRolesRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalUserUpdateRequest;
import com.samplus.smartrecrutare.localauth.dto.ManagerEmployerAssignmentRequest;
import com.samplus.smartrecrutare.localauth.service.LocalUserAdminService;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.security.RoluriAplicatie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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

/** Expune operatiile administrative pentru utilizatorii LocalAuth. */
@RestController
@Validated
@RequestMapping("/api/admin/local-users")
@PreAuthorize(RoluriAplicatie.ADMIN)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administrare LocalAuth", description = "Management pentru utilizatori locali stocati in baza de date")
public class LocalUserAdminController {
    private final LocalUserAdminService service;

    public LocalUserAdminController(LocalUserAdminService service) {
        this.service = service;
    }

    @Operation(summary = "Creeaza un utilizator local")
    @PostMapping
    public ResponseEntity<LocalUserResponse> create(@Valid @RequestBody LocalUserCreateRequest request) {
        LocalUserResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Listeaza utilizatorii locali")
    @GetMapping
    public ResponseEntity<PaginaModel<LocalUserResponse>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(service.list(PageRequest.of(page, size)));
    }

    @Operation(summary = "Obtine un utilizator local")
    @GetMapping("/{id}")
    public ResponseEntity<LocalUserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Actualizeaza profilul unui utilizator local")
    @PutMapping("/{id}")
    public ResponseEntity<LocalUserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LocalUserUpdateRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Inlocuieste rolurile unui utilizator local")
    @PutMapping("/{id}/roles")
    public ResponseEntity<LocalUserResponse> replaceRoles(
            @PathVariable Long id,
            @Valid @RequestBody LocalUserRolesRequest request
    ) {
        return ResponseEntity.ok(service.replaceRoles(id, request));
    }

    @Operation(summary = "Reseteaza parola unui utilizator local")
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> replacePassword(
            @PathVariable Long id,
            @Valid @RequestBody LocalUserPasswordRequest request
    ) {
        service.replacePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atribuie un angajator unui manager local")
    @PostMapping("/{id}/managed-employers")
    public ResponseEntity<LocalUserResponse> assignEmployer(
            @PathVariable Long id,
            @Valid @RequestBody ManagerEmployerAssignmentRequest request
    ) {
        return ResponseEntity.ok(service.assignEmployer(id, request));
    }

    @Operation(summary = "Sterge atribuirea unui angajator pentru managerul local")
    @DeleteMapping("/{id}/managed-employers/{employerId}")
    public ResponseEntity<LocalUserResponse> removeEmployer(
            @PathVariable Long id,
            @PathVariable Long employerId
    ) {
        return ResponseEntity.ok(service.removeEmployer(id, employerId));
    }
}
