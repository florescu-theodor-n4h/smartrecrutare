package com.samplus.smartrecrutare.analytics.service.impl;

import com.samplus.smartrecrutare.Candidat;
import com.samplus.smartrecrutare.DepozitCandidati;
import com.samplus.smartrecrutare.analytics.domain.ProfilAnaliticCandidat;
import com.samplus.smartrecrutare.analytics.exception.ConflictAnaliticException;
import com.samplus.smartrecrutare.analytics.exception.ResursaAnaliticaNegasitaException;
import com.samplus.smartrecrutare.analytics.mapper.MapperAnalitice;
import com.samplus.smartrecrutare.analytics.repository.ProfilAnaliticCandidatRepository;
import com.samplus.smartrecrutare.analytics.service.NormalizatorTextAnalitic;
import com.samplus.smartrecrutare.analytics.service.ServiciuProfiluriAnalitice;
import com.samplus.smartrecrutare.analytics.service.ValidatorVersiuneAnalitica;
import com.samplus.smartrecrutare.models.ActualizareProfilCandidatRequest;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.ProfilCandidatRequest;
import com.samplus.smartrecrutare.models.ProfilCandidatResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementare tranzactionala pentru profilurile analitice. */
@Service
public class ServiciuProfiluriAnaliticeImplicit implements ServiciuProfiluriAnalitice {

    private final DepozitCandidati depozitCandidati;
    private final ProfilAnaliticCandidatRepository profilRepository;
    private final NormalizatorTextAnalitic normalizator;
    private final ValidatorVersiuneAnalitica validatorVersiune;
    private final MapperAnalitice mapper;

    public ServiciuProfiluriAnaliticeImplicit(
            DepozitCandidati depozitCandidati,
            ProfilAnaliticCandidatRepository profilRepository,
            NormalizatorTextAnalitic normalizator,
            ValidatorVersiuneAnalitica validatorVersiune,
            MapperAnalitice mapper
    ) {
        this.depozitCandidati = depozitCandidati;
        this.profilRepository = profilRepository;
        this.normalizator = normalizator;
        this.validatorVersiune = validatorVersiune;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ProfilCandidatResponse creare(Long candidatId, ProfilCandidatRequest request) {
        if (profilRepository.existsByCandidatId(candidatId)) {
            throw new ConflictAnaliticException("Candidatul are deja un profil analitic");
        }
        Candidat candidat = depozitCandidati.findById(candidatId)
                .orElseThrow(() -> new ResursaAnaliticaNegasitaException(
                        "Candidatul " + candidatId + " nu exista"
                ));
        ProfilAnaliticCandidat profil = ProfilAnaliticCandidat.creare(candidat);
        aplica(profil, request);
        profilRepository.saveAndFlush(profil);
        return mapper.profil(profil);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilCandidatResponse gasire(Long candidatId) {
        return mapper.profil(profilComplet(candidatId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaModel<ProfilCandidatResponse> listare(Pageable pageable) {
        var pagina = profilRepository.findAllByOrderByUpdatedAtDesc(pageable);
        var continut = pagina.getContent().stream().map(mapper::profil).toList();
        return mapper.pagina(pagina, continut);
    }

    @Override
    @Transactional
    public ProfilCandidatResponse inlocuire(Long candidatId, ActualizareProfilCandidatRequest request) {
        ProfilAnaliticCandidat profil = profilComplet(candidatId);
        validatorVersiune.verifica("Profilul candidatului", candidatId, profil.getVersion(), request.versiune());
        aplica(profil, request.profil());
        profilRepository.flush();
        return mapper.profil(profil);
    }

    @Override
    @Transactional
    public void stergere(Long candidatId, Long versiune) {
        ProfilAnaliticCandidat profil = profilComplet(candidatId);
        validatorVersiune.verifica("Profilul candidatului", candidatId, profil.getVersion(), versiune);
        profilRepository.delete(profil);
        profilRepository.flush();
    }

    private ProfilAnaliticCandidat profilComplet(Long candidatId) {
        return profilRepository.gasesteCompletDupaCandidatId(candidatId)
                .orElseThrow(() -> new ResursaAnaliticaNegasitaException(
                        "Profilul analitic pentru candidatul " + candidatId + " nu exista"
                ));
    }

    private void aplica(ProfilAnaliticCandidat profil, ProfilCandidatRequest request) {
        profil.inlocuire(
                normalizator.multime(request.abilitati()),
                normalizator.multime(request.locatiiPreferate()),
                normalizator.textOptional(request.tipContractPreferat()),
                normalizator.multime(request.cuvinteCheie())
        );
    }
}
