package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Notificare independenta de limba.
 * Frontend-ul traduce identificatorul si aplica valorile in substituenti.
 */
@Schema(description = "Notificare bazata pe identificator de traducere si substituenti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificareResponse {

    @Schema(description = "Identificatorul notificarii")
    private UUID id;

    @Schema(description = "Destinatarul tehnic", example = "ion.popescu@email.com")
    private String destinatar;

    @Schema(description = "Identificatorul din catalogul de traduceri", example = "analytics.match.available")
    private String mesajId;

    @Schema(description = "Valori pentru substituentii mesajului")
    private Map<String, String> substituenti;

    @Schema(description = "Starea notificarii")
    private StareNotificare stare;

    @Schema(description = "Identificatorul rezultatului asociat")
    private UUID rezultatPotrivireId;

    @Schema(description = "Momentul publicarii")
    private Instant creatLa;

    @Schema(description = "Momentul citirii")
    private Instant cititLa;

    @Schema(description = "Versiunea optimista")
    private Long versiune;
}
