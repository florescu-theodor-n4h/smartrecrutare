package com.samplus.smartrecrutare.analytics.repository;

import com.samplus.smartrecrutare.analytics.domain.ProfilAnaliticCandidat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Acces persistent pentru profilurile analitice. */
public interface ProfilAnaliticCandidatRepository extends JpaRepository<ProfilAnaliticCandidat, UUID> {

    @EntityGraph(attributePaths = {"candidat", "abilitati", "locatiiPreferate", "cuvinteCheie"})
    @Query("select profil from ProfilAnaliticCandidat profil where profil.candidat.id = :candidatId")
    Optional<ProfilAnaliticCandidat> gasesteCompletDupaCandidatId(@Param("candidatId") Long candidatId);

    @EntityGraph(attributePaths = "candidat")
    @Query("select distinct profil from ProfilAnaliticCandidat profil")
    List<ProfilAnaliticCandidat> gasesteToatePentruPotrivire();

    boolean existsByCandidatId(Long candidatId);

    @EntityGraph(attributePaths = "candidat")
    Page<ProfilAnaliticCandidat> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
