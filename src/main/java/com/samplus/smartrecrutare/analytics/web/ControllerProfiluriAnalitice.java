package com.samplus.smartrecrutare.analytics.web;

import com.samplus.smartrecrutare.analytics.security.RoluriAnalitice;
import com.samplus.smartrecrutare.analytics.service.ServiciuProfiluriAnalitice;
import com.samplus.smartrecrutare.models.ActualizareProfilCandidatRequest;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.ProfilCandidatRequest;
import com.samplus.smartrecrutare.models.ProfilCandidatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

/** API subtire pentru profilurile folosite de algoritmul de potrivire. */
@RestController
@Validated
@RequestMapping("/api/admin/analytics/candidate-profiles")
@PreAuthorize(RoluriAnalitice.ADMIN_SAU_RECRUITER)
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Administrare profiluri analitice",
        description = "CRUD pentru abilitatile si preferintele candidatilor"
)
public class ControllerProfiluriAnalitice {

    private final ServiciuProfiluriAnalitice serviciu;

    public ControllerProfiluriAnalitice(ServiciuProfiluriAnalitice serviciu) {
        this.serviciu = serviciu;
    }

    @Operation(
            summary = "Creeaza profilul analitic al candidatului",
            description = "Leaga un singur profil persistent de candidatul existent. Necesita rol de administrator sau recruiter."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profil creat",
                    content = @Content(schema = @Schema(implementation = ProfilCandidatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide"),
            @ApiResponse(responseCode = "404", description = "Candidat inexistent"),
            @ApiResponse(responseCode = "409", description = "Profil deja existent")
    })
    @PostMapping("/{candidatId}")
    public ResponseEntity<ProfilCandidatResponse> creare(
            @Parameter(description = "Identificatorul candidatului", required = true, example = "12")
            @PathVariable Long candidatId,
            @Valid @RequestBody ProfilCandidatRequest request
    ) {
        ProfilCandidatResponse raspuns = serviciu.creare(candidatId, request);
        URI locatie = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(locatie).body(raspuns);
    }

    @Operation(summary = "Listeaza profilurile analitice", description = "Returneaza o pagina ordonata dupa ultima modificare.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina de profiluri",
                    content = @Content(schema = @Schema(implementation = PaginaModel.class))),
            @ApiResponse(responseCode = "400", description = "Paginare invalida")
    })
    @GetMapping
    public ResponseEntity<PaginaModel<ProfilCandidatResponse>> listare(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(serviciu.listare(PageRequest.of(page, size)));
    }

    @Operation(summary = "Obtine profilul unui candidat", description = "Cauta profilul dupa identificatorul candidatului, nu dupa id-ul intern al profilului.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil gasit",
                    content = @Content(schema = @Schema(implementation = ProfilCandidatResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profil inexistent")
    })
    @GetMapping("/{candidatId}")
    public ResponseEntity<ProfilCandidatResponse> gasire(@PathVariable Long candidatId) {
        return ResponseEntity.ok(serviciu.gasire(candidatId));
    }

    @Operation(summary = "Inlocuieste profilul candidatului", description = "Foloseste versiunea pentru detectarea actualizarilor concurente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil actualizat",
                    content = @Content(schema = @Schema(implementation = ProfilCandidatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide"),
            @ApiResponse(responseCode = "404", description = "Profil inexistent"),
            @ApiResponse(responseCode = "409", description = "Versiune conflictuala")
    })
    @PutMapping("/{candidatId}")
    public ResponseEntity<ProfilCandidatResponse> inlocuire(
            @PathVariable Long candidatId,
            @Valid @RequestBody ActualizareProfilCandidatRequest request
    ) {
        return ResponseEntity.ok(serviciu.inlocuire(candidatId, request));
    }

    @Operation(summary = "Sterge profilul candidatului", description = "Operatie rezervata administratorului si protejata prin versiune optimista.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profil sters"),
            @ApiResponse(responseCode = "404", description = "Profil inexistent"),
            @ApiResponse(responseCode = "409", description = "Versiune conflictuala")
    })
    @DeleteMapping("/{candidatId}")
    @PreAuthorize(RoluriAnalitice.ADMIN)
    public ResponseEntity<Void> stergere(
            @PathVariable Long candidatId,
            @RequestParam @NotNull @PositiveOrZero Long version
    ) {
        serviciu.stergere(candidatId, version);
        return ResponseEntity.noContent().build();
    }
}
