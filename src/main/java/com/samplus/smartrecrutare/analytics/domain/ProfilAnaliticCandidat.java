package com.samplus.smartrecrutare.analytics.domain;

import com.samplus.smartrecrutare.Candidat;
import com.samplus.smartrecrutare.security.AuditableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Profil separat de candidatul operational, optimizat pentru potrivire. */
@Entity
@Table(
        name = "analytics_candidate_profiles",
        indexes = @Index(name = "idx_analytics_profile_candidate", columnList = "candidate_id", unique = true)
)
public class ProfilAnaliticCandidat extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "candidate_id",
            nullable = false,
            updatable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_analytics_profile_candidate")
    )
    private Candidat candidat;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "analytics_candidate_skills",
            joinColumns = @JoinColumn(name = "profile_id", foreignKey = @ForeignKey(name = "fk_analytics_skill_profile"))
    )
    @Column(name = "skill", nullable = false, length = 80)
    @BatchSize(size = 50)
    private Set<String> abilitati = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "analytics_candidate_locations",
            joinColumns = @JoinColumn(name = "profile_id", foreignKey = @ForeignKey(name = "fk_analytics_location_profile"))
    )
    @Column(name = "location", nullable = false, length = 120)
    @BatchSize(size = 50)
    private Set<String> locatiiPreferate = new LinkedHashSet<>();

    @Column(name = "preferred_contract", length = 80)
    private String tipContractPreferat;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "analytics_candidate_keywords",
            joinColumns = @JoinColumn(name = "profile_id", foreignKey = @ForeignKey(name = "fk_analytics_keyword_profile"))
    )
    @Column(name = "keyword", nullable = false, length = 80)
    @BatchSize(size = 50)
    private Set<String> cuvinteCheie = new LinkedHashSet<>();

    protected ProfilAnaliticCandidat() {
        // Constructor necesar pentru JPA.
    }

    private ProfilAnaliticCandidat(Candidat candidat) {
        this.candidat = candidat;
    }

    public static ProfilAnaliticCandidat creare(Candidat candidat) {
        return new ProfilAnaliticCandidat(candidat);
    }

    public void inlocuire(
            Set<String> abilitati,
            Set<String> locatiiPreferate,
            String tipContractPreferat,
            Set<String> cuvinteCheie
    ) {
        this.abilitati = new LinkedHashSet<>(abilitati);
        this.locatiiPreferate = new LinkedHashSet<>(locatiiPreferate);
        this.tipContractPreferat = tipContractPreferat;
        this.cuvinteCheie = new LinkedHashSet<>(cuvinteCheie);
    }

    public UUID getId() {
        return id;
    }

    public Candidat getCandidat() {
        return candidat;
    }

    public Set<String> getAbilitati() {
        return Set.copyOf(abilitati);
    }

    public Set<String> getLocatiiPreferate() {
        return Set.copyOf(locatiiPreferate);
    }

    public String getTipContractPreferat() {
        return tipContractPreferat;
    }

    public Set<String> getCuvinteCheie() {
        return Set.copyOf(cuvinteCheie);
    }
}
