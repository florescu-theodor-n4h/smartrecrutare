package com.samplus.smartrecrutare.analytics.service.impl;

import com.samplus.smartrecrutare.analytics.service.ProcesatorAnaliticeFundal;
import com.samplus.smartrecrutare.analytics.service.RegistruExecutiiAnalitice;
import com.samplus.smartrecrutare.analytics.service.ServiciuExecutiiAnalitice;
import com.samplus.smartrecrutare.models.ExecutieAnaliticaResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Orchestrator fara tranzactie lunga pentru pornirea asincrona. */
@Service
public class ServiciuExecutiiAnaliticeImplicit implements ServiciuExecutiiAnalitice {

    private final RegistruExecutiiAnalitice registru;
    private final ProcesatorAnaliticeFundal procesator;

    public ServiciuExecutiiAnaliticeImplicit(
            RegistruExecutiiAnalitice registru,
            ProcesatorAnaliticeFundal procesator
    ) {
        this.registru = registru;
        this.procesator = procesator;
    }

    @Override
    public ExecutieAnaliticaResponse solicitaExecutie() {
        ExecutieAnaliticaResponse executie = registru.creareInAsteptare();
        procesator.proceseaza(executie.id());
        return executie;
    }

    @Override
    public ExecutieAnaliticaResponse gasire(UUID executieId) {
        return registru.gasire(executieId);
    }

    @Override
    public PaginaModel<ExecutieAnaliticaResponse> listare(Pageable pageable) {
        return registru.listare(pageable);
    }
}
