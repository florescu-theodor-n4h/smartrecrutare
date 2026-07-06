package com.samplus.smartrecrutare.analytics.repository;

import com.samplus.smartrecrutare.analytics.domain.NotificareAnalitica;
import com.samplus.smartrecrutare.models.StareNotificare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Acces persistent pentru mesajele localizabile. */
public interface NotificareAnaliticaRepository extends JpaRepository<NotificareAnalitica, UUID> {

    Page<NotificareAnalitica> findByDestinatarIgnoreCaseOrderByCreatedAtDesc(
            String destinatar,
            Pageable pageable
    );

    Page<NotificareAnalitica> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"substituenti", "rezultatPotrivire"})
    Optional<NotificareAnalitica> findByIdAndDestinatarIgnoreCase(UUID id, String destinatar);

    long countByStare(StareNotificare stare);

    boolean existsByRezultatPotrivireIdAndMesajId(UUID rezultatPotrivireId, String mesajId);
}
