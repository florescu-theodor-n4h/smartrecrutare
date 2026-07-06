package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Notificare independenta de limba.
 * Frontend-ul traduce identificatorul si aplica valorile in substituenti.
 */
@Schema(description = "Notificare bazata pe identificator de traducere si substituenti")
public record NotificareResponse(
        @Schema(description = "Identificatorul notificarii") UUID id,
        @Schema(description = "Destinatarul tehnic", example = "ion.popescu@email.com") String destinatar,
        @Schema(description = "Identificatorul din catalogul de traduceri", example = "analytics.match.available") String mesajId,
        @Schema(description = "Valori pentru substituentii mesajului") Map<String, String> substituenti,
        @Schema(description = "Starea notificarii") StareNotificare stare,
        @Schema(description = "Identificatorul rezultatului asociat") UUID rezultatPotrivireId,
        @Schema(description = "Momentul publicarii") Instant creatLa,
        @Schema(description = "Momentul citirii") Instant cititLa,
        @Schema(description = "Versiunea optimista") Long versiune
) {
}
