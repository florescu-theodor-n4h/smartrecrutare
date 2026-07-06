package com.samplus.smartrecrutare.analytics.service.impl;

import com.samplus.smartrecrutare.analytics.domain.ExecutieAnalitica;
import com.samplus.smartrecrutare.analytics.exception.ConflictAnaliticException;
import com.samplus.smartrecrutare.analytics.exception.ResursaAnaliticaNegasitaException;
import com.samplus.smartrecrutare.analytics.mapper.MapperAnalitice;
import com.samplus.smartrecrutare.analytics.repository.ExecutieAnaliticaRepository;
import com.samplus.smartrecrutare.analytics.service.RegistruExecutiiAnalitice;
import com.samplus.smartrecrutare.models.ExecutieAnaliticaResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.StareExecutieAnalitica;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Registru JPA cu tranzactii independente pentru actualizarea progresului. */
@Service
public class RegistruExecutiiAnaliticeJpa implements RegistruExecutiiAnalitice {

    private static final List<StareExecutieAnalitica> STARI_ACTIVE = List.of(
            StareExecutieAnalitica.IN_ASTEPTARE,
            StareExecutieAnalitica.IN_EXECUTIE
    );

    private final ExecutieAnaliticaRepository repository;
    private final MapperAnalitice mapper;

    public RegistruExecutiiAnaliticeJpa(
            ExecutieAnaliticaRepository repository,
            MapperAnalitice mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ExecutieAnaliticaResponse creareInAsteptare() {
        if (repository.existsByStareIn(STARI_ACTIVE)) {
            throw new ConflictAnaliticException("Exista deja o executie analitica activa");
        }
        ExecutieAnalitica executie = ExecutieAnalitica.inAsteptare();
        repository.saveAndFlush(executie);
        return mapper.executie(executie);
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutieAnaliticaResponse gasire(UUID executieId) {
        return mapper.executie(executie(executieId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaModel<ExecutieAnaliticaResponse> listare(Pageable pageable) {
        var pagina = repository.findAllByOrderByCreatedAtDesc(pageable);
        return mapper.pagina(
                pagina,
                pagina.getContent().stream().map(mapper::executie).collect(Collectors.toList())
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcheazaPornita(UUID executieId) {
        executie(executieId).pornire();
        repository.flush();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcheazaFinalizata(UUID executieId, long evaluate, long pestePrag, long notificari) {
        executie(executieId).finalizare(evaluate, pestePrag, notificari);
        repository.flush();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcheazaEsuata(UUID executieId, String codEroare) {
        executie(executieId).esec(codEroare);
        repository.flush();
    }

    private ExecutieAnalitica executie(UUID executieId) {
        return repository.findById(executieId)
                .orElseThrow(() -> new ResursaAnaliticaNegasitaException(
                        "Executia " + executieId + " nu exista"
                ));
    }
}
