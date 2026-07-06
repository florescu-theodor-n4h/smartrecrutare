package com.samplus.smartrecrutare.analytics.service.impl;

import com.samplus.smartrecrutare.Candidat;
import com.samplus.smartrecrutare.Job;
import com.samplus.smartrecrutare.analytics.domain.NotificareAnalitica;
import com.samplus.smartrecrutare.analytics.domain.RezultatPotrivire;
import com.samplus.smartrecrutare.analytics.domain.TiparPotrivire;
import com.samplus.smartrecrutare.analytics.matching.RezultatPersistare;
import com.samplus.smartrecrutare.analytics.matching.ScorPotrivire;
import com.samplus.smartrecrutare.analytics.notification.MesajeNotificare;
import com.samplus.smartrecrutare.analytics.repository.NotificareAnaliticaRepository;
import com.samplus.smartrecrutare.analytics.repository.RezultatPotrivireRepository;
import com.samplus.smartrecrutare.analytics.service.PersistentaRezultatPotrivire;
import com.samplus.smartrecrutare.models.StarePotrivire;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

/** Persistenta izolata pentru fiecare pereche evaluata. */
@Service
public class PersistentaRezultatPotrivireJpa implements PersistentaRezultatPotrivire {

    private final RezultatPotrivireRepository rezultatRepository;
    private final NotificareAnaliticaRepository notificareRepository;
    private final EntityManager entityManager;

    public PersistentaRezultatPotrivireJpa(
            RezultatPotrivireRepository rezultatRepository,
            NotificareAnaliticaRepository notificareRepository,
            EntityManager entityManager
    ) {
        this.rezultatRepository = rezultatRepository;
        this.notificareRepository = notificareRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RezultatPersistare salveaza(ScorPotrivire scor) {
        RezultatPotrivire rezultat = rezultatRepository
                .findByCandidatIdAndJobIdAndTiparId(
                        scor.getProfil().getCandidatId(),
                        scor.getJob().getJobId(),
                        scor.getTipar().getTiparId()
                )
                .orElseGet(() -> RezultatPotrivire.creare(
                        entityManager.getReference(Candidat.class, scor.getProfil().getCandidatId()),
                        entityManager.getReference(Job.class, scor.getJob().getJobId()),
                        entityManager.getReference(TiparPotrivire.class, scor.getTipar().getTiparId())
                ));
        StarePotrivire stareAnterioara = rezultat.getStare();
        rezultat.evaluare(
                scor.getTotal(),
                scor.getAbilitati(),
                scor.getLocatie(),
                scor.getContract(),
                scor.getCuvinteCheie(),
                scor.getStare()
        );
        rezultatRepository.saveAndFlush(rezultat);

        boolean publica = scor.getStare() == StarePotrivire.PESTE_PRAG
                && stareAnterioara != StarePotrivire.PESTE_PRAG
                && !notificareRepository.existsByRezultatPotrivireIdAndMesajId(
                        rezultat.getId(),
                        MesajeNotificare.POTRIVIRE_DISPONIBILA
                );
        if (publica) {
            NotificareAnalitica notificare = NotificareAnalitica.creare(
                    scor.getProfil().getEmailCandidat().trim().toLowerCase(Locale.ROOT),
                    MesajeNotificare.POTRIVIRE_DISPONIBILA,
                    Map.of(
                            MesajeNotificare.NUME_CANDIDAT, substituent(scor.getProfil().getNumeCandidat()),
                            MesajeNotificare.TITLU_JOB, substituent(scor.getJob().getTitlu()),
                            MesajeNotificare.COMPANIE, substituent(scor.getJob().getCompanie()),
                            MesajeNotificare.SCOR, Integer.toString(scor.getTotal()),
                            MesajeNotificare.REZULTAT_ID, rezultat.getId().toString()
                    ),
                    rezultat
            );
            notificareRepository.save(notificare);
        }
        return new RezultatPersistare(scor.getStare() == StarePotrivire.PESTE_PRAG, publica);
    }

    private String substituent(String valoare) {
        return valoare == null ? "" : valoare;
    }
}
