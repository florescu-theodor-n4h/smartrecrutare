package com.samplus.smartrecrutare;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Serviciu atomic enterprise pentru gestionarea joburilor.
 *
 * <p>Toate operațiile de scriere sunt tranzacționale și atomice: fie se execută
 * complet, fie se rollback în caz de eroare necontrolată. Logica de business
 * este complet izolată de stratul HTTP (controller) și de stratul de date
 * (repository), respectând principiul Single Responsibility.</p>
 *
 * <p>Convenții aplicare consistentă cu {@code ServiciuCandidat}:</p>
 * <ul>
 *   <li>Crearea forțează {@code id = null} pentru a preveni merge-ul accidental.</li>
 *   <li>Ștergerea returnează {@code false} dacă entitatea nu există (idempotentă, no-throw).</li>
 *   <li>Actualizarea aruncă {@link EntityNotFoundException} dacă id-ul lipsește.</li>
 *   <li>Interogările folosesc {@code TxType.SUPPORTS} — participă în tranzacție existentă,
 *       dar nu creează una nouă dacă nu există.</li>
 * </ul>
 */
@Service
public class ServiciuJoburi {
    private static final Logger log = LoggerFactory.getLogger(ServiciuJoburi.class);
    private final DepozitJoburi depozitJoburi;

    public ServiciuJoburi(DepozitJoburi depozitJoburi) {
        this.depozitJoburi = depozitJoburi;
    }

    // -------------------------------------------------------------------------
    // CREARE
    // -------------------------------------------------------------------------

    /**
     * Creează un job nou în baza de date.
     *
     * <p>Id-ul este resetat la {@code null} indiferent de ce trimite clientul,
     * pentru a forța un INSERT și a evita merge-ul accidental al unei entități existente.
     * Câmpurile de audit ({@code creatLa}, {@code actualizatLa}) sunt setate automat
     * prin {@code @PrePersist} pe entitate.</p>
     *
     * @param job datele jobului de creat
     * @return entitatea persistată cu id-ul generat și timestamps de audit populate
     * @throws IllegalArgumentException dacă {@code job} este null
     * @throws IllegalArgumentException dacă {@code titlu} sau {@code companie} lipsesc
     */
    @Transactional
    public Job creare(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job-ul nu poate fi null");
        }
        validareObligatorie(job);

        job.setId(null); // Forțăm INSERT, nu UPDATE/merge accidental

        log.info("Creare job: titlu='{}' companie='{}'", job.getTitlu(), job.getCompanie());
        Job salvat = depozitJoburi.save(job);
        log.info("Job creat cu succes: id={}", salvat.getId());

        return salvat;
    }

    // -------------------------------------------------------------------------
    // ACTUALIZARE
    // -------------------------------------------------------------------------

    /**
     * Actualizează un job existent (update parțial — doar câmpurile non-null din DTO).
     *
     * <p>Câmpurile de audit ({@code creatLa}, {@code actualizatLa}) și id-ul nu sunt
     * niciodată suprascrise din DTO. {@code actualizatLa} este refreshed automat
     * prin {@code @PreUpdate} pe entitate.</p>
     *
     * @param id  identificatorul jobului de actualizat
     * @param dto obiect cu noile valori (câmpurile null sunt ignorate)
     * @return entitatea actualizată și persistată
     * @throws EntityNotFoundException  dacă nu există niciun job cu {@code id}-ul dat
     * @throws IllegalArgumentException dacă {@code id} sau {@code dto} sunt null
     */
    @Transactional
    public Job actualizare(Long id, Job dto) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("DTO-ul jobului nu poate fi null");
        }

        log.info("Actualizare job: id={}", id);

        Job existent = depozitJoburi.findById(id)
                .orElseThrow(() -> {
                    log.error("Job inexistent la actualizare: id={}", id);
                    return new EntityNotFoundException("Job inexistent cu id=" + id);
                });

        // Actualizare parțială — câmpurile null din DTO nu suprascriu datele existente
        if (dto.getTitlu() != null)       existent.setTitlu(dto.getTitlu());
        if (dto.getDescriere() != null)   existent.setDescriere(dto.getDescriere());
        if (dto.getCompanie() != null)    existent.setCompanie(dto.getCompanie());
        if (dto.getLocatie() != null)     existent.setLocatie(dto.getLocatie());
        if (dto.getSalariu() != null)     existent.setSalariu(dto.getSalariu());
        if (dto.getTipContract() != null) existent.setTipContract(dto.getTipContract());
        // activ este boolean primitiv — îl actualizăm întotdeauna dacă DTO-ul e prezent
        existent.setActiv(dto.isActiv());

        Job actualizat = depozitJoburi.save(existent);
        log.info("Job actualizat cu succes: id={} titlu='{}'", actualizat.getId(), actualizat.getTitlu());

        return actualizat;
    }

    // -------------------------------------------------------------------------
    // STERGERE
    // -------------------------------------------------------------------------

    /**
     * Șterge un job după id.
     *
     * <p>Dacă nu există niciun job cu id-ul dat, returnează {@code false} fără a
     * arunca excepție — comportament deliberat pentru idempotență (DELETE idempotent).</p>
     *
     * @param id identificatorul unic al jobului
     * @return {@code true} dacă a fost găsit și șters, {@code false} altfel
     * @throws IllegalArgumentException dacă {@code id} este null
     */
    @Transactional
    public boolean stergere(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }

        if (!depozitJoburi.existsById(id)) {
            log.warn("Stergere ignorata — job inexistent: id={}", id);
            return false;
        }

        depozitJoburi.deleteById(id);
        log.info("Job sters: id={}", id);
        return true;
    }

    // -------------------------------------------------------------------------
    // INTEROGARI (read-only)
    // -------------------------------------------------------------------------

    /**
     * Returnează toate joburile din baza de date (active și inactive).
     *
     * <p>Durata query-ului și numărul de rezultate sunt logați pentru monitorizare.</p>
     *
     * @return colecție (posibil goală) cu toate joburile
     */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Collection<Job> getTateJoburile() {
        final long start = System.currentTimeMillis();

        List<Job> joburi = depozitJoburi.findAll();

        final long durata = System.currentTimeMillis() - start;
        log.info("GetToateJoburile t={}ms count={}", durata, joburi.size());

        return joburi;
    }

    /**
     * Caută un job după id.
     *
     * @param id identificatorul jobului
     * @return {@link Optional} cu jobul găsit, sau gol dacă nu există
     * @throws IllegalArgumentException dacă {@code id} este null
     */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Optional<Job> gasireById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }

        log.debug("Cautare job: id={}", id);
        return depozitJoburi.findById(id);
    }

    /**
     * Returnează toate joburile cu {@code activ = true}.
     *
     * @return lista joburilor active, posibil goală
     */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public List<Job> getJoburiActive() {
        final long start = System.currentTimeMillis();

        List<Job> joburi = depozitJoburi.findByActivTrue();

        final long durata = System.currentTimeMillis() - start;
        log.info("GetJoburiActive t={}ms count={}", durata, joburi.size());

        return joburi;
    }

    /**
     * Caută joburi al căror titlu conține fragmentul dat (case-insensitive).
     *
     * @param fragment textul de căutat în titlu
     * @return lista joburilor găsite, posibil goală
     * @throws IllegalArgumentException dacă {@code fragment} este null sau gol
     */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public List<Job> cautareDupaTitlu(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            throw new IllegalArgumentException("Fragmentul de căutare nu poate fi null/gol");
        }

        log.debug("Cautare joburi dupa titlu: fragment='{}'", fragment);
        return depozitJoburi.findByTitluContainingIgnoreCase(fragment);
    }

    // -------------------------------------------------------------------------
    // Validare internă
    // -------------------------------------------------------------------------

    /**
     * Validează câmpurile obligatorii ale unui job înainte de persistare.
     *
     * @param job entitatea de validat
     * @throws IllegalArgumentException dacă titlul sau compania lipsesc/sunt goale
     */
    private void validareObligatorie(Job job) {
        if (job.getTitlu() == null || job.getTitlu().isBlank()) {
            throw new IllegalArgumentException("Titlul jobului este obligatoriu");
        }
        if (job.getCompanie() == null || job.getCompanie().isBlank()) {
            throw new IllegalArgumentException("Compania jobului este obligatorie");
        }
    }
}