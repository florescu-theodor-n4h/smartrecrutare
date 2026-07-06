package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cerere versionata pentru marcarea notificarii drept citita. */
@Schema(description = "Control optimist pentru schimbarea starii notificarii")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcareNotificareCititaRequest {
    @Schema(description = "Versiunea curenta", example = "0")
    @NotNull
    @PositiveOrZero
    private Long versiune;
}
