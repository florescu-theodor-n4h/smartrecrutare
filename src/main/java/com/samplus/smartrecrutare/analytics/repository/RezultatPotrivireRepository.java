package com.samplus.smartrecrutare.analytics.repository;

import com.samplus.smartrecrutare.analytics.domain.RezultatPotrivire;
import com.samplus.smartrecrutare.models.StarePotrivire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Acces persistent pentru rezultatele calculate. */
public interface RezultatPotrivireRepository extends JpaRepository<RezultatPotrivire, UUID> {

    @EntityGraph(attributePaths = {"candidat", "job", "tipar"})
    Optional<RezultatPotrivire> findByCandidatIdAndJobIdAndTiparId(
            Long candidatId,
            Long jobId,
            UUID tiparId
    );

    @EntityGraph(attributePaths = {"candidat", "job", "tipar"})
    Page<RezultatPotrivire> findAllByOrderByScorTotalDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"candidat", "job", "tipar"})
    Page<RezultatPotrivire> findByStareOrderByScorTotalDesc(StarePotrivire stare, Pageable pageable);

    long countByStare(StarePotrivire stare);

    boolean existsByTiparId(UUID tiparId);
}
