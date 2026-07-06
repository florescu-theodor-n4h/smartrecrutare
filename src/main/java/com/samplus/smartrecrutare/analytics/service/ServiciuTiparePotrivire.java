package com.samplus.smartrecrutare.analytics.service;

import com.samplus.smartrecrutare.models.ActualizareTiparPotrivireRequest;
import com.samplus.smartrecrutare.models.CreareTiparPotrivireRequest;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.TiparPotrivireResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/** Contract pentru configurarea tiparelor de catre administrator. */
public interface ServiciuTiparePotrivire {
    TiparPotrivireResponse creare(CreareTiparPotrivireRequest request);

    TiparPotrivireResponse gasire(UUID tiparId);

    PaginaModel<TiparPotrivireResponse> listare(Pageable pageable);

    TiparPotrivireResponse inlocuire(UUID tiparId, ActualizareTiparPotrivireRequest request);

    void stergere(UUID tiparId, Long versiune);
}
