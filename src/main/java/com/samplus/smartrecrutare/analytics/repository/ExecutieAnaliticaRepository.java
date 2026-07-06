package com.samplus.smartrecrutare.analytics.repository;

import com.samplus.smartrecrutare.analytics.domain.ExecutieAnalitica;
import com.samplus.smartrecrutare.models.StareExecutieAnalitica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

/** Acces persistent pentru jurnalul executarilor de fundal. */
public interface ExecutieAnaliticaRepository extends JpaRepository<ExecutieAnalitica, UUID> {
    boolean existsByStareIn(Collection<StareExecutieAnalitica> stari);

    Page<ExecutieAnalitica> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
