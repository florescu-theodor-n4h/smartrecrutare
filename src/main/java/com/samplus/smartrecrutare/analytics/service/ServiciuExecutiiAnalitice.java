package com.samplus.smartrecrutare.analytics.service;

import com.samplus.smartrecrutare.models.ExecutieAnaliticaResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/** Contract public pentru pornirea si urmarirea proceselor asincrone. */
public interface ServiciuExecutiiAnalitice {
    ExecutieAnaliticaResponse solicitaExecutie();

    ExecutieAnaliticaResponse gasire(UUID executieId);

    PaginaModel<ExecutieAnaliticaResponse> listare(Pageable pageable);
}
