package com.samplus.smartrecrutare;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Serviciu atomic enterprise pentru gestionarea candidaților.
 *
 * <p>Toate operațiile sunt tranzacționale și atomic: fie se execută complet,
 * fie se rollback în caz de eroare. Logica de business este separată de
 * controller (SRP) și de repository (repository pattern).</p>
 *
 * <p>Convenții:</p>
 * <ul>
 *   <li>Crearea forțează {@code id = null} pentru a preveni merge-ul accidental.</li>
 *   <li>Ștergerea returnează {@code false} dacă entitatea nu există (no-throw by design).</li>
 *   <li>Actualizarea aruncă {@link EntityNotFoundException} dacă id-ul lipsește.</li>
 * </ul>
 */
@Service
public class ServiciuCandidat {

    private static final Logger log = LoggerFactory.getLogger(ServiciuCandidat.class);

    private final DepozitCandidati depozitCandidati;

    public ServiciuCandidat(DepozitCandidati depozitCandidati) {
        this.depozitCandidati = depozitCandidati;
    }

    // -------------------------------------------------------------------------
    // CREARE
    // -------------------------------------------------------------------------

    /**
     * Creează un candidat nou în baza de date.
     *
     * <p>Id-ul este resetat la {@code null} indiferent de ce trimite clientul,
     * pentru a evita merge-ul accidental al unei entități existente.</p>
     *
     * @param candidat datele candidatului de creat
     * @return entitatea persistată cu id-ul generat
     * @throws IllegalArgumentException dacă {@code candidat} este null
     */
    @Transactional
    public Candidat creare(Candidat candidat) {
        if (candidat == null) {
            throw new IllegalArgumentException("Candidatul nu poate fi null");
        }

        candidat.setId(null); // Forțăm INSERT, nu UPDATE/merge accidental

        log.info("Creare candidat: numePrenume={}", candidat.getNumePrenume());
        Candidat salvat = depozitCandidati.save(candidat);
        log.info("Candidat creat cu succes: id={}", salvat.getId());

        return salvat;
    }

    // -------------------------------------------------------------------------
    // STERGERE
    // -------------------------------------------------------------------------

    /**
     * Șterge un candidat după nume și prenume.
     *
     * <p>Dacă nu există niciun candidat cu numele dat, returnează {@code false}
     * fără a arunca excepție — comportament deliberat pentru idempotență.</p>
     *
     * @param numePrenume numele complet al candidatului de șters
     * @return {@code true} dacă a fost găsit și șters, {@code false} altfel
     * @throws IllegalArgumentException dacă {@code numePrenume} este null sau gol
     */
    @Transactional
    public boolean stergere(String numePrenume) {
        if (numePrenume == null || numePrenume.isBlank()) {
            throw new IllegalArgumentException("numePrenume nu poate fi null/gol");
        }

        final var candidatOpt = depozitCandidati.findByNumePrenume(numePrenume);

        if (candidatOpt.isEmpty()) {
            log.warn("Stergere ignorata — candidat inexistent: numePrenume={}", numePrenume);
            return false;
        }

        depozitCandidati.deleteAll(candidatOpt);
        //depozitCandidati.delete(candidatOpt.get());
        log.info("Candidat(i) sters: numePrenume={}", numePrenume);
        return true;
    }

    /**
     * Șterge un candidat după id.
     *
     * <p>Variantă mai sigură și mai performantă decât ștergerea după nume,
     * deoarece operează pe cheia primară.</p>
     *
     * @param id identificatorul unic al candidatului
     * @return {@code true} dacă a fost găsit și șters, {@code false} altfel
     * @throws IllegalArgumentException dacă {@code id} este null
     */
    @Transactional
    public boolean stergereById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }

        if (!depozitCandidati.existsById(id)) {
            log.warn("Stergere ignorata — candidat inexistent: id={}", id);
            return false;
        }

        depozitCandidati.deleteById(id);
        log.info("Candidat sters: id={}", id);
        return true;
    }

    // -------------------------------------------------------------------------
    // ACTUALIZARE
    // -------------------------------------------------------------------------

    /**
     * Actualizează un candidat existent (update parțial pe câmpurile permise).
     *
     * <p>Doar câmpurile {@code numePrenume}, {@code mail} și {@code tel} sunt
     * actualizabile. Id-ul și alte câmpuri de audit rămân neschimbate.</p>
     *
     * @param id  identificatorul candidatului de actualizat
     * @param dto obiect cu noile valori
     * @return entitatea actualizată și persistată
     * @throws EntityNotFoundException  dacă nu există niciun candidat cu {@code id}-ul dat
     * @throws IllegalArgumentException dacă {@code id} sau {@code dto} sunt null
     */
    @Transactional
    public Candidat actualizare(Long id, Candidat dto) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("DTO-ul candidatului nu poate fi null");
        }

        log.info("Actualizare candidat: id={}", id);

        Candidat existent = depozitCandidati.findById(id)
                .orElseThrow(() -> {
                    log.error("Candidat inexistent la actualizare: id={}", id);
                    return new EntityNotFoundException("Candidat inexistent cu id=" + id);
                });

        // Câmpuri actualizabile — extensibil cu validare per câmp
        if (dto.getNumePrenume() != null) {
            existent.setNumePrenume(dto.getNumePrenume());
        }
        if (dto.getMail() != null) {
            existent.setMail(dto.getMail());
        }
        if (dto.getTel() != null) {
            existent.setTel(dto.getTel());
        }

        Candidat actualizat = depozitCandidati.save(existent);
        log.info("Candidat actualizat cu succes: id={}", actualizat.getId());

        return actualizat;
    }

    // -------------------------------------------------------------------------
    // INTEROGARI (read-only)
    // -------------------------------------------------------------------------

    /**
     * Returnează toți candidații din baza de date.
     *
     * <p>Operația este read-only — nu modifică starea bazei de date.
     * Durata query-ului este logată pentru monitorizare.</p>
     *
     * @return colecție (posibil goală) cu toți candidații
     */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Collection<Candidat> getTotiCandidatii() {
        final long start = System.currentTimeMillis();

        Collection<Candidat> candidati = depozitCandidati.findAll();

        final long durata = System.currentTimeMillis() - start;
        log.info("GetTotiCandidatii t={}ms count={}", durata, candidati.size());

        return candidati;
    }

    /**
     * Caută un candidat după id.
     *
     * @param id identificatorul candidatului
     * @return {@link Optional} cu candidatul găsit, sau gol dacă nu există
     * @throws IllegalArgumentException dacă {@code id} este null
     */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Optional<Candidat> gasireById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }

        log.debug("Cautare candidat: id={}", id);
        return depozitCandidati.findById(id);
    }

    /**
     * Caută un candidat după nume și prenume.
     *
     * @param numePrenume numele complet al candidatului
     * @return {@link Optional} cu candidatul găsit, sau gol dacă nu există
     * @throws IllegalArgumentException dacă {@code numePrenume} este null sau gol
     */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Set<Candidat> gasireByNumePrenume(String numePrenume) {
        if (numePrenume == null || numePrenume.isBlank()) {
            throw new IllegalArgumentException("numePrenume nu poate fi null/gol");
        }

        log.debug("Cautare candidat: numePrenume={}", numePrenume);
        return depozitCandidati.findByNumePrenume(numePrenume)
              //  .stream();
        ;
    }
}