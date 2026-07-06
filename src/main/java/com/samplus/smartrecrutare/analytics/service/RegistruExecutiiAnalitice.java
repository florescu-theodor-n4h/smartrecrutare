package com.samplus.smartrecrutare.analytics.service;

import com.samplus.smartrecrutare.models.ExecutieAnaliticaResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/** Gestioneaza tranzactiile scurte ale jurnalului de executie. */
public interface RegistruExecutiiAnalitice {
    ExecutieAnaliticaResponse creareInAsteptare();

    ExecutieAnaliticaResponse gasire(UUID executieId);

    PaginaModel<ExecutieAnaliticaResponse> listare(Pageable pageable);

    void marcheazaPornita(UUID executieId);

    void marcheazaFinalizata(UUID executieId, long evaluate, long pestePrag, long notificari);

    void marcheazaEsuata(UUID executieId, String codEroare);
}
