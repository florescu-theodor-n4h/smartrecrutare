package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Raspuns standard pentru resurse paginate. */
@Schema(description = "Pagina standard de rezultate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginaModel<T> {
    @Schema(description = "Elementele paginii curente")
    private List<T> continut;

    @Schema(description = "Numarul paginii, pornind de la zero", example = "0")
    private int pagina;

    @Schema(description = "Dimensiunea paginii", example = "20")
    private int dimensiune;

    @Schema(description = "Numarul total de elemente", example = "125")
    private long totalElemente;

    @Schema(description = "Numarul total de pagini", example = "7")
    private int totalPagini;
}
