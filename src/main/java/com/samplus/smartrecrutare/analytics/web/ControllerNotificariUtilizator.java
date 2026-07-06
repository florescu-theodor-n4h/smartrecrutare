package com.samplus.smartrecrutare.analytics.web;

import com.samplus.smartrecrutare.analytics.security.ResolverDestinatarCurent;
import com.samplus.smartrecrutare.analytics.security.RoluriAnalitice;
import com.samplus.smartrecrutare.analytics.service.ServiciuNotificari;
import com.samplus.smartrecrutare.models.MarcareNotificareCititaRequest;
import com.samplus.smartrecrutare.models.NotificareResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** API pentru centrul de notificari al utilizatorului autentificat. */
@RestController
@Validated
@RequestMapping("/api/analytics/notifications/me")
@PreAuthorize(RoluriAnalitice.AUTENTIFICAT)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notificari utilizator", description = "Mesaje localizate de frontend pe baza mesajId si substituenti")
public class ControllerNotificariUtilizator {

    private final ServiciuNotificari serviciu;
    private final ResolverDestinatarCurent resolverDestinatar;

    public ControllerNotificariUtilizator(
            ServiciuNotificari serviciu,
            ResolverDestinatarCurent resolverDestinatar
    ) {
        this.serviciu = serviciu;
        this.resolverDestinatar = resolverDestinatar;
    }

    @Operation(
            summary = "Listeaza notificarile utilizatorului curent",
            description = "Destinatarul este derivat din claim-ul email al JWT; nu poate fi ales prin query string."
    )
    @ApiResponse(responseCode = "200", description = "Pagina de notificari",
            content = @Content(schema = @Schema(implementation = PaginaModel.class)))
    @GetMapping
    public ResponseEntity<PaginaModel<NotificareResponse>> listare(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        String destinatar = resolverDestinatar.rezolva(authentication);
        return ResponseEntity.ok(serviciu.listarePentru(destinatar, PageRequest.of(page, size)));
    }

    @Operation(summary = "Marcheaza notificarea drept citita", description = "Operatia este idempotenta si verifica proprietarul si versiunea.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificare actualizata",
                    content = @Content(schema = @Schema(implementation = NotificareResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notificare inexistenta pentru utilizator"),
            @ApiResponse(responseCode = "409", description = "Versiune conflictuala")
    })
    @PutMapping("/{notificareId}/read-state")
    public ResponseEntity<NotificareResponse> marcheazaCitita(
            Authentication authentication,
            @Parameter(description = "Identificatorul notificarii", required = true)
            @PathVariable UUID notificareId,
            @Valid @RequestBody MarcareNotificareCititaRequest request
    ) {
        String destinatar = resolverDestinatar.rezolva(authentication);
        return ResponseEntity.ok(serviciu.marcheazaCitita(destinatar, notificareId, request));
    }
}
