package com.samplus.smartrecrutare.analytics.web;

import com.samplus.smartrecrutare.analytics.security.RoluriAnalitice;
import com.samplus.smartrecrutare.analytics.service.ServiciuTiparePotrivire;
import com.samplus.smartrecrutare.models.ActualizareTiparPotrivireRequest;
import com.samplus.smartrecrutare.models.CreareTiparPotrivireRequest;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.TiparPotrivireResponse;
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
import java.util.UUID;

/** API administrativa pentru regulile ponderate ale algoritmului. */
@RestController
@Validated
@RequestMapping("/api/admin/analytics/patterns")
@PreAuthorize(RoluriAnalitice.ADMIN)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administrare tipare de potrivire", description = "CRUD versionat pentru ponderi si praguri")
public class ControllerTiparePotrivire {

    private final ServiciuTiparePotrivire serviciu;

    public ControllerTiparePotrivire(ServiciuTiparePotrivire serviciu) {
        this.serviciu = serviciu;
    }

    @Operation(summary = "Creeaza un tipar", description = "Ponderile celor patru criterii trebuie sa insumeze exact 100.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tipar creat",
                    content = @Content(schema = @Schema(implementation = TiparPotrivireResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ponderi sau date invalide"),
            @ApiResponse(responseCode = "409", description = "Nume deja folosit")
    })
    @PostMapping
    public ResponseEntity<TiparPotrivireResponse> creare(
            @Valid @RequestBody CreareTiparPotrivireRequest request
    ) {
        TiparPotrivireResponse raspuns = serviciu.creare(request);
        URI locatie = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{tiparId}")
                .buildAndExpand(raspuns.id())
                .toUri();
        return ResponseEntity.created(locatie).body(raspuns);
    }

    @Operation(summary = "Listeaza tiparele", description = "Include tiparele active si inactive pentru pagina administratorului.")
    @ApiResponse(responseCode = "200", description = "Pagina de tipare",
            content = @Content(schema = @Schema(implementation = PaginaModel.class)))
    @GetMapping
    public ResponseEntity<PaginaModel<TiparPotrivireResponse>> listare(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(serviciu.listare(PageRequest.of(page, size)));
    }

    @Operation(summary = "Obtine un tipar", description = "Returneaza inclusiv metadatele de audit si versiunea optimista.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipar gasit",
                    content = @Content(schema = @Schema(implementation = TiparPotrivireResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tipar inexistent")
    })
    @GetMapping("/{tiparId}")
    public ResponseEntity<TiparPotrivireResponse> gasire(
            @Parameter(description = "Identificator UUID al tiparului", required = true)
            @PathVariable UUID tiparId
    ) {
        return ResponseEntity.ok(serviciu.gasire(tiparId));
    }

    @Operation(summary = "Inlocuieste un tipar", description = "Actualizeaza atomic toate ponderile, pragul si starea de activare.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipar actualizat",
                    content = @Content(schema = @Schema(implementation = TiparPotrivireResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ponderi invalide"),
            @ApiResponse(responseCode = "404", description = "Tipar inexistent"),
            @ApiResponse(responseCode = "409", description = "Versiune sau nume conflictual")
    })
    @PutMapping("/{tiparId}")
    public ResponseEntity<TiparPotrivireResponse> inlocuire(
            @PathVariable UUID tiparId,
            @Valid @RequestBody ActualizareTiparPotrivireRequest request
    ) {
        return ResponseEntity.ok(serviciu.inlocuire(tiparId, request));
    }

    @Operation(summary = "Sterge un tipar nefolosit", description = "Tiparele cu rezultate istorice trebuie dezactivate, nu sterse.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tipar sters"),
            @ApiResponse(responseCode = "404", description = "Tipar inexistent"),
            @ApiResponse(responseCode = "409", description = "Tipar folosit sau versiune conflictuala")
    })
    @DeleteMapping("/{tiparId}")
    public ResponseEntity<Void> stergere(
            @PathVariable UUID tiparId,
            @RequestParam @NotNull @PositiveOrZero Long version
    ) {
        serviciu.stergere(tiparId, version);
        return ResponseEntity.noContent().build();
    }
}
