package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** Reprezentarea administrativa a unui tipar auditat. */
@Schema(description = "Tipar de potrivire configurabil si auditat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiparPotrivireResponse {
    @Schema(description = "Identificatorul tiparului") private UUID id;
    @Schema(description = "Numele unic") private String nume;
    @Schema(description = "Descrierea administrativa") private String descriere;
    @Schema(description = "Ponderea abilitatilor") private int pondereAbilitati;
    @Schema(description = "Ponderea locatiei") private int pondereLocatie;
    @Schema(description = "Ponderea contractului") private int pondereContract;
    @Schema(description = "Ponderea cuvintelor cheie") private int pondereCuvinteCheie;
    @Schema(description = "Pragul de notificare") private int pragNotificare;
    @Schema(description = "Starea de activare") private boolean activ;
    @Schema(description = "Data crearii", accessMode = Schema.AccessMode.READ_ONLY) private Instant creatLa;
    @Schema(description = "Autorul crearii", accessMode = Schema.AccessMode.READ_ONLY) private String creatDe;
    @Schema(description = "Data ultimei modificari", accessMode = Schema.AccessMode.READ_ONLY) private Instant modificatLa;
    @Schema(description = "Autorul ultimei modificari", accessMode = Schema.AccessMode.READ_ONLY) private String modificatDe;
    @Schema(description = "Versiunea optimista", accessMode = Schema.AccessMode.READ_ONLY) private Long versiune;
}
