package com.samplus.smartrecrutare.analytics.service.impl;

import com.samplus.smartrecrutare.analytics.domain.TiparPotrivire;
import com.samplus.smartrecrutare.analytics.exception.CerereAnaliticaInvalidaException;
import com.samplus.smartrecrutare.analytics.exception.ConflictAnaliticException;
import com.samplus.smartrecrutare.analytics.exception.ResursaAnaliticaNegasitaException;
import com.samplus.smartrecrutare.analytics.mapper.MapperAnalitice;
import com.samplus.smartrecrutare.analytics.repository.RezultatPotrivireRepository;
import com.samplus.smartrecrutare.analytics.repository.TiparPotrivireRepository;
import com.samplus.smartrecrutare.analytics.service.ServiciuTiparePotrivire;
import com.samplus.smartrecrutare.analytics.service.ValidatorVersiuneAnalitica;
import com.samplus.smartrecrutare.models.ActualizareTiparPotrivireRequest;
import com.samplus.smartrecrutare.models.CreareTiparPotrivireRequest;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.TiparPotrivireResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Implementare tranzactionala pentru tiparele de potrivire. */
@Service
public class ServiciuTiparePotrivireImplicit implements ServiciuTiparePotrivire {

    private final TiparPotrivireRepository tiparRepository;
    private final RezultatPotrivireRepository rezultatRepository;
    private final ValidatorVersiuneAnalitica validatorVersiune;
    private final MapperAnalitice mapper;

    public ServiciuTiparePotrivireImplicit(
            TiparPotrivireRepository tiparRepository,
            RezultatPotrivireRepository rezultatRepository,
            ValidatorVersiuneAnalitica validatorVersiune,
            MapperAnalitice mapper
    ) {
        this.tiparRepository = tiparRepository;
        this.rezultatRepository = rezultatRepository;
        this.validatorVersiune = validatorVersiune;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TiparPotrivireResponse creare(CreareTiparPotrivireRequest request) {
        valideaza(request);
        if (tiparRepository.existsByNumeIgnoreCase(request.nume().trim())) {
            throw new ConflictAnaliticException("Exista deja un tipar cu acelasi nume");
        }
        TiparPotrivire tipar = TiparPotrivire.creare(
                request.nume().trim(),
                textOptional(request.descriere()),
                request.pondereAbilitati(),
                request.pondereLocatie(),
                request.pondereContract(),
                request.pondereCuvinteCheie(),
                request.pragNotificare(),
                request.activ()
        );
        tiparRepository.saveAndFlush(tipar);
        return mapper.tipar(tipar);
    }

    @Override
    @Transactional(readOnly = true)
    public TiparPotrivireResponse gasire(UUID tiparId) {
        return mapper.tipar(tipar(tiparId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaModel<TiparPotrivireResponse> listare(Pageable pageable) {
        var pagina = tiparRepository.findAllByOrderByNumeAsc(pageable);
        return mapper.pagina(pagina, pagina.getContent().stream().map(mapper::tipar).toList());
    }

    @Override
    @Transactional
    public TiparPotrivireResponse inlocuire(UUID tiparId, ActualizareTiparPotrivireRequest request) {
        TiparPotrivire tipar = tipar(tiparId);
        validatorVersiune.verifica("Tiparul", tiparId, tipar.getVersion(), request.versiune());
        valideaza(request.tipar());
        tiparRepository.findByNumeIgnoreCase(request.tipar().nume().trim())
                .filter(altTipar -> !altTipar.getId().equals(tiparId))
                .ifPresent(altTipar -> {
                    throw new ConflictAnaliticException("Exista deja un tipar cu acelasi nume");
                });
        aplica(tipar, request.tipar());
        tiparRepository.flush();
        return mapper.tipar(tipar);
    }

    @Override
    @Transactional
    public void stergere(UUID tiparId, Long versiune) {
        TiparPotrivire tipar = tipar(tiparId);
        validatorVersiune.verifica("Tiparul", tiparId, tipar.getVersion(), versiune);
        if (rezultatRepository.existsByTiparId(tiparId)) {
            throw new ConflictAnaliticException("Tiparul are rezultate istorice si nu poate fi sters");
        }
        tiparRepository.delete(tipar);
        tiparRepository.flush();
    }

    private TiparPotrivire tipar(UUID tiparId) {
        return tiparRepository.findById(tiparId)
                .orElseThrow(() -> new ResursaAnaliticaNegasitaException(
                        "Tiparul " + tiparId + " nu exista"
                ));
    }

    private void valideaza(CreareTiparPotrivireRequest request) {
        int total = request.pondereAbilitati()
                + request.pondereLocatie()
                + request.pondereContract()
                + request.pondereCuvinteCheie();
        if (total != 100) {
            throw new CerereAnaliticaInvalidaException("Ponderile tiparului trebuie sa insumeze 100");
        }
    }

    private void aplica(TiparPotrivire tipar, CreareTiparPotrivireRequest request) {
        tipar.inlocuire(
                request.nume().trim(),
                textOptional(request.descriere()),
                request.pondereAbilitati(),
                request.pondereLocatie(),
                request.pondereContract(),
                request.pondereCuvinteCheie(),
                request.pragNotificare(),
                request.activ()
        );
    }

    private String textOptional(String valoare) {
        return valoare == null || valoare.isBlank() ? null : valoare.trim();
    }
}
