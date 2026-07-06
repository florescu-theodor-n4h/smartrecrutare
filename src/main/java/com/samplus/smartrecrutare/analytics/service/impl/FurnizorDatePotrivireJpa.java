package com.samplus.smartrecrutare.analytics.service.impl;

import com.samplus.smartrecrutare.DepozitJoburi;
import com.samplus.smartrecrutare.analytics.matching.DateJobPotrivire;
import com.samplus.smartrecrutare.analytics.matching.DateProfilPotrivire;
import com.samplus.smartrecrutare.analytics.matching.DateTiparPotrivire;
import com.samplus.smartrecrutare.analytics.matching.LotDatePotrivire;
import com.samplus.smartrecrutare.analytics.repository.ProfilAnaliticCandidatRepository;
import com.samplus.smartrecrutare.analytics.repository.TiparPotrivireRepository;
import com.samplus.smartrecrutare.analytics.service.FurnizorDatePotrivire;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Citeste si copiaza datele in obiecte imutabile. */
@Service
public class FurnizorDatePotrivireJpa implements FurnizorDatePotrivire {

    private final ProfilAnaliticCandidatRepository profilRepository;
    private final DepozitJoburi depozitJoburi;
    private final TiparPotrivireRepository tiparRepository;

    public FurnizorDatePotrivireJpa(
            ProfilAnaliticCandidatRepository profilRepository,
            DepozitJoburi depozitJoburi,
            TiparPotrivireRepository tiparRepository
    ) {
        this.profilRepository = profilRepository;
        this.depozitJoburi = depozitJoburi;
        this.tiparRepository = tiparRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LotDatePotrivire citeste() {
        List<DateProfilPotrivire> profiluri = profilRepository.gasesteToatePentruPotrivire().stream()
                .map(profil -> new DateProfilPotrivire(
                        profil.getCandidat().getId(),
                        profil.getCandidat().getNumePrenume(),
                        profil.getCandidat().getMail(),
                        profil.getAbilitati(),
                        profil.getLocatiiPreferate(),
                        profil.getTipContractPreferat(),
                        profil.getCuvinteCheie()
                ))
                .collect(Collectors.toList());
        List<DateJobPotrivire> joburi = depozitJoburi.findByActivTrue().stream()
                .map(job -> new DateJobPotrivire(
                        job.getId(),
                        job.getTitlu(),
                        job.getDescriere(),
                        job.getCompanie(),
                        job.getLocatie(),
                        job.getTipContract()
                ))
                .collect(Collectors.toList());
        List<DateTiparPotrivire> tipare = tiparRepository.findByActivTrueOrderByNumeAsc().stream()
                .map(tipar -> new DateTiparPotrivire(
                        tipar.getId(),
                        tipar.getNume(),
                        tipar.getPondereAbilitati(),
                        tipar.getPondereLocatie(),
                        tipar.getPondereContract(),
                        tipar.getPondereCuvinteCheie(),
                        tipar.getPragNotificare()
                ))
                .collect(Collectors.toList());
        return new LotDatePotrivire(profiluri, joburi, tipare);
    }
}
