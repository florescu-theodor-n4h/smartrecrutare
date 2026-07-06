package com.samplus.smartrecrutare.analytics.domain;

import com.samplus.smartrecrutare.Candidat;
import com.samplus.smartrecrutare.Job;
import com.samplus.smartrecrutare.models.StarePotrivire;
import com.samplus.smartrecrutare.security.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** Rezultat unic pentru combinatia candidat, job si tipar. */
@Getter
@Entity
@Table(
        name = "analytics_match_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_analytics_match_candidate_job_pattern",
                columnNames = {"candidate_id", "job_id", "pattern_id"}
        ),
        indexes = {
                @Index(name = "idx_analytics_match_score", columnList = "total_score"),
                @Index(name = "idx_analytics_match_state", columnList = "state"),
                @Index(name = "idx_analytics_match_candidate", columnList = "candidate_id")
        }
)
public class RezultatPotrivire extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_analytics_match_candidate"))
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_analytics_match_job"))
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pattern_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_analytics_match_pattern"))
    private TiparPotrivire tipar;

    @Column(name = "total_score", nullable = false)
    private int scorTotal;

    @Column(name = "skill_score", nullable = false)
    private int scorAbilitati;

    @Column(name = "location_score", nullable = false)
    private int scorLocatie;

    @Column(name = "contract_score", nullable = false)
    private int scorContract;

    @Column(name = "keyword_score", nullable = false)
    private int scorCuvinteCheie;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 30)
    private StarePotrivire stare;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatLa;

    protected RezultatPotrivire() {
        // Constructor necesar pentru JPA.
    }

    public static RezultatPotrivire creare(Candidat candidat, Job job, TiparPotrivire tipar) {
        RezultatPotrivire rezultat = new RezultatPotrivire();
        rezultat.candidat = candidat;
        rezultat.job = job;
        rezultat.tipar = tipar;
        return rezultat;
    }

    public void evaluare(
            int scorTotal,
            int scorAbilitati,
            int scorLocatie,
            int scorContract,
            int scorCuvinteCheie,
            StarePotrivire stare
    ) {
        this.scorTotal = scorTotal;
        this.scorAbilitati = scorAbilitati;
        this.scorLocatie = scorLocatie;
        this.scorContract = scorContract;
        this.scorCuvinteCheie = scorCuvinteCheie;
        this.stare = stare;
        this.evaluatLa = Instant.now();
    }

}
