package com.samplus.smartrecrutare.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Raspuns standard pentru resurse paginate. */
@Schema(description = "Pagina standard de rezultate")
public record PaginaModel<T>(
        @Schema(description = "Elementele paginii curente") List<T> continut,
        @Schema(description = "Numarul paginii, pornind de la zero", example = "0") int pagina,
        @Schema(description = "Dimensiunea paginii", example = "20") int dimensiune,
        @Schema(description = "Numarul total de elemente", example = "125") long totalElemente,
        @Schema(description = "Numarul total de pagini", example = "7") int totalPagini
) {
}
