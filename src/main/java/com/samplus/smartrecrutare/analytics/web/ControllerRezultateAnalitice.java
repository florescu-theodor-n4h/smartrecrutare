package com.samplus.smartrecrutare.analytics.web;

import com.samplus.smartrecrutare.analytics.security.RoluriAnalitice;
import com.samplus.smartrecrutare.analytics.service.ServiciuRezultateAnalitice;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.RezultatPotrivireResponse;
import com.samplus.smartrecrutare.models.StarePotrivire;
import com.samplus.smartrecrutare.models.TablouAdministrareAnaliticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API de citire pentru rezultate si indicatori agregati. */
@RestController
@Validated
@RequestMapping("/api/admin/analytics")
@PreAuthorize(RoluriAnalitice.ADMIN)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Rezultate analitice", description = "Rezultate paginate si indicatori pentru administrare")
public class ControllerRezultateAnalitice {

    private final ServiciuRezultateAnalitice serviciu;

    public ControllerRezultateAnalitice(ServiciuRezultateAnalitice serviciu) {
        this.serviciu = serviciu;
    }

    @Operation(
            summary = "Listeaza rezultatele de potrivire",
            description = "Filtrul stare este optional; rezultatele sunt ordonate descrescator dupa scor."
    )
    @ApiResponse(responseCode = "200", description = "Pagina de rezultate",
            content = @Content(schema = @Schema(implementation = PaginaModel.class)))
    @GetMapping("/matches")
    public ResponseEntity<PaginaModel<RezultatPotrivireResponse>> rezultate(
            @Parameter(description = "Filtru optional fata de prag")
            @RequestParam(required = false) StarePotrivire stare,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(serviciu.listare(stare, PageRequest.of(page, size)));
    }

    @Operation(
            summary = "Obtine tabloul de administrare",
            description = "Calculeaza indicatori prin interogari agregate, fara a incarca toate entitatile."
    )
    @ApiResponse(responseCode = "200", description = "Indicatori calculati",
            content = @Content(schema = @Schema(implementation = TablouAdministrareAnaliticeResponse.class)))
    @GetMapping("/dashboard")
    public ResponseEntity<TablouAdministrareAnaliticeResponse> tablou() {
        return ResponseEntity.ok(serviciu.tablouAdministrare());
    }
}
