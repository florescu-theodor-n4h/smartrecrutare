package com.samplus.smartrecrutare.analytics.web;

import com.samplus.smartrecrutare.analytics.security.RoluriAnalitice;
import com.samplus.smartrecrutare.analytics.service.ServiciuNotificari;
import com.samplus.smartrecrutare.models.NotificareResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.PublicareNotificareRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** API administrativa pentru inspectarea si publicarea mesajelor. */
@RestController
@Validated
@RequestMapping("/api/admin/analytics/notifications")
@PreAuthorize(RoluriAnalitice.ADMIN)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administrare notificari", description = "Publicare prin chei de traducere, fara text hardcodat intr-o limba")
public class ControllerAdministrareNotificari {

    private final ServiciuNotificari serviciu;

    public ControllerAdministrareNotificari(ServiciuNotificari serviciu) {
        this.serviciu = serviciu;
    }

    @Operation(summary = "Listeaza toate notificarile", description = "Endpoint pentru audit si suport operational.")
    @ApiResponse(responseCode = "200", description = "Pagina de notificari",
            content = @Content(schema = @Schema(implementation = PaginaModel.class)))
    @GetMapping
    public ResponseEntity<PaginaModel<NotificareResponse>> listare(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(serviciu.listareAdministrativa(PageRequest.of(page, size)));
    }

    @Operation(
            summary = "Publica o notificare localizabila",
            description = "Accepta doar mesajId si substituenti; traducerea este responsabilitatea frontend-ului."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificare publicata",
                    content = @Content(schema = @Schema(implementation = NotificareResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identificator sau substituenti invalizi")
    })
    @PostMapping
    public ResponseEntity<NotificareResponse> publicare(
            @Valid @RequestBody PublicareNotificareRequest request
    ) {
        NotificareResponse raspuns = serviciu.publicare(request);
        URI locatie = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{notificareId}")
                .buildAndExpand(raspuns.getId())
                .toUri();
        return ResponseEntity.created(locatie).body(raspuns);
    }
}
