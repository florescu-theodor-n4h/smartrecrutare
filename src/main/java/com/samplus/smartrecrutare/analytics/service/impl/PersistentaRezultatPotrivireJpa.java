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
                        scor.profil().candidatId(),
                        scor.job().jobId(),
                        scor.tipar().tiparId()
                )
                .orElseGet(() -> RezultatPotrivire.creare(
                        entityManager.getReference(Candidat.class, scor.profil().candidatId()),
                        entityManager.getReference(Job.class, scor.job().jobId()),
                        entityManager.getReference(TiparPotrivire.class, scor.tipar().tiparId())
                ));
        StarePotrivire stareAnterioara = rezultat.getStare();
        rezultat.evaluare(
                scor.total(),
                scor.abilitati(),
                scor.locatie(),
                scor.contract(),
                scor.cuvinteCheie(),
                scor.stare()
        );
        rezultatRepository.saveAndFlush(rezultat);

        boolean publica = scor.stare() == StarePotrivire.PESTE_PRAG
                && stareAnterioara != StarePotrivire.PESTE_PRAG
                && !notificareRepository.existsByRezultatPotrivireIdAndMesajId(
                        rezultat.getId(),
                        MesajeNotificare.POTRIVIRE_DISPONIBILA
                );
        if (publica) {
            NotificareAnalitica notificare = NotificareAnalitica.creare(
                    scor.profil().emailCandidat().trim().toLowerCase(Locale.ROOT),
                    MesajeNotificare.POTRIVIRE_DISPONIBILA,
                    Map.of(
                            MesajeNotificare.NUME_CANDIDAT, substituent(scor.profil().numeCandidat()),
                            MesajeNotificare.TITLU_JOB, substituent(scor.job().titlu()),
                            MesajeNotificare.COMPANIE, substituent(scor.job().companie()),
                            MesajeNotificare.SCOR, Integer.toString(scor.total()),
                            MesajeNotificare.REZULTAT_ID, rezultat.getId().toString()
                    ),
                    rezultat
            );
            notificareRepository.save(notificare);
        }
        return new RezultatPersistare(scor.stare() == StarePotrivire.PESTE_PRAG, publica);
    }

    private String substituent(String valoare) {
        return valoare == null ? "" : valoare;
    }
}
