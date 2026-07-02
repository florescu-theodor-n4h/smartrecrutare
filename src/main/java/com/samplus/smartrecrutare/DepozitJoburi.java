package com.samplus.smartrecrutare;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Depozitul (repository) JPA pentru entitatea {@link Job}.
 *
 * <p>Extinde {@link JpaRepository} și furnizează metode derivate din nume
 * plus query-uri JPQL pentru căutări specializate.</p>
 *
 * <p>Metodele CRUD de bază ({@code save}, {@code findById}, {@code findAll},
 * {@code deleteById}, {@code existsById}) sunt moștenite automat.</p>
 */
@Repository
public interface DepozitJoburi extends JpaRepository<Job, Long> {

    /**
     * Returnează toate joburile active (activ = true).
     *
     * @return lista joburilor active, posibil goală
     */
    List<Job> findByActivTrue();

    /**
     * Returnează toate joburile publicate de o anumită companie.
     *
     * @param companie numele companiei
     * @return lista joburilor companiei, posibil goală
     */
    List<Job> findByCompanieIgnoreCase(String companie);

    /**
     * Returnează toate joburile dintr-o anumită locație.
     *
     * @param locatie locația căutată
     * @return lista joburilor din locație, posibil goală
     */
    List<Job> findByLocatieIgnoreCase(String locatie);

    /**
     * Caută joburi al căror titlu conține textul dat (case-insensitive).
     *
     * @param fragment fragmentul de text de căutat în titlu
     * @return lista joburilor găsite, posibil goală
     */
    List<Job> findByTitluContainingIgnoreCase(String fragment);

    /**
     * Returnează toate joburile active ale unei companii.
     *
     * <p>Util pentru listarea ofertelor curente ale unui angajator specific.</p>
     *
     * @param companie numele companiei
     * @return lista joburilor active ale companiei
     */
    @Query("SELECT j FROM Job j WHERE j.activ = true AND LOWER(j.companie) = LOWER(:companie)")
    List<Job> findActiveByCompanie(@Param("companie") String companie);

    /**
     * Returnează numărul de joburi active din sistem.
     *
     * @return numărul total de joburi cu {@code activ = true}
     */
    long countByActivTrue();
}