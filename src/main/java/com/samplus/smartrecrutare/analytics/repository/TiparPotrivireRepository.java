package com.samplus.smartrecrutare.analytics.repository;

import com.samplus.smartrecrutare.analytics.domain.TiparPotrivire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Acces persistent pentru tiparele configurate de administrator. */
public interface TiparPotrivireRepository extends JpaRepository<TiparPotrivire, UUID> {
    List<TiparPotrivire> findByActivTrueOrderByNumeAsc();

    Page<TiparPotrivire> findAllByOrderByNumeAsc(Pageable pageable);

    boolean existsByNumeIgnoreCase(String nume);

    Optional<TiparPotrivire> findByNumeIgnoreCase(String nume);

    long countByActivTrue();
}
