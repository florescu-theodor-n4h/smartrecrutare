package com.samplus.smartrecrutare.analytics.web;

import com.samplus.smartrecrutare.analytics.security.RoluriAnalitice;
import com.samplus.smartrecrutare.analytics.service.ServiciuExecutiiAnalitice;
import com.samplus.smartrecrutare.models.ExecutieAnaliticaResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/** API administrativa pentru comenzi asincrone si starea lor. */
@RestController
@Validated
@RequestMapping("/api/admin/analytics/runs")
@PreAuthorize(RoluriAnalitice.ADMIN)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Executii analitice", description = "Pornire asincrona si monitorizare fara blocarea cererii HTTP")
public class ControllerExecutiiAnalitice {

    private final ServiciuExecutiiAnalitice serviciu;

    public ControllerExecutiiAnalitice(ServiciuExecutiiAnalitice serviciu) {
        this.serviciu = serviciu;
    }

    @Operation(
            summary = "Porneste recalcularea potrivirilor",
            description = "Inregistreaza executia si raspunde imediat cu 202. Procesarea continua pe executorul dedicat."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Executie acceptata",
                    content = @Content(schema = @Schema(implementation = ExecutieAnaliticaResponse.class))),
            @ApiResponse(responseCode = "409", description = "Alta executie este deja activa")
    })
    @PostMapping
    public ResponseEntity<ExecutieAnaliticaResponse> pornire() {
        ExecutieAnaliticaResponse raspuns = serviciu.solicitaExecutie();
        URI locatie = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{executieId}")
                .buildAndExpand(raspuns.id())
                .toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(locatie).body(raspuns);
    }

    @Operation(summary = "Listeaza executarile", description = "Returneaza executarile recente, inclusiv codurile tehnice de eroare.")
    @ApiResponse(responseCode = "200", description = "Pagina de executii",
            content = @Content(schema = @Schema(implementation = PaginaModel.class)))
    @GetMapping
    public ResponseEntity<PaginaModel<ExecutieAnaliticaResponse>> listare(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(serviciu.listare(PageRequest.of(page, size)));
    }

    @Operation(summary = "Obtine starea unei executii", description = "Endpoint de polling pentru pagina administratorului.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Executie gasita",
                    content = @Content(schema = @Schema(implementation = ExecutieAnaliticaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Executie inexistenta")
    })
    @GetMapping("/{executieId}")
    public ResponseEntity<ExecutieAnaliticaResponse> gasire(
            @Parameter(description = "Identificatorul executiei", required = true)
            @PathVariable UUID executieId
    ) {
        return ResponseEntity.ok(serviciu.gasire(executieId));
    }
}
