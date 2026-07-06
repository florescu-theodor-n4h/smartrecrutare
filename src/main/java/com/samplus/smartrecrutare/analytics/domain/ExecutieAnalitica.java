package com.samplus.smartrecrutare.analytics.domain;

import com.samplus.smartrecrutare.models.StareExecutieAnalitica;
import com.samplus.smartrecrutare.security.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Jurnalul persistent al unei executii de fundal. */
@Entity
@Table(
        name = "analytics_runs",
        indexes = @Index(name = "idx_analytics_run_status_created", columnList = "status,created_at")
)
public class ExecutieAnalitica extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StareExecutieAnalitica stare;

    @Column(name = "evaluated_pairs", nullable = false)
    private long perechiEvaluate;

    @Column(name = "matches_above_threshold", nullable = false)
    private long potriviriPestePrag;

    @Column(name = "published_notifications", nullable = false)
    private long notificariPublicate;

    @Column(name = "error_code", length = 100)
    private String codEroare;

    @Column(name = "started_at")
    private Instant pornitLa;

    @Column(name = "completed_at")
    private Instant finalizatLa;

    protected ExecutieAnalitica() {
        // Constructor necesar pentru JPA.
    }

    public static ExecutieAnalitica inAsteptare() {
        ExecutieAnalitica executie = new ExecutieAnalitica();
        executie.stare = StareExecutieAnalitica.IN_ASTEPTARE;
        return executie;
    }

    public void pornire() {
        this.stare = StareExecutieAnalitica.IN_EXECUTIE;
        this.pornitLa = Instant.now();
        this.codEroare = null;
    }

    public void finalizare(long perechiEvaluate, long potriviriPestePrag, long notificariPublicate) {
        this.stare = StareExecutieAnalitica.FINALIZATA;
        this.perechiEvaluate = perechiEvaluate;
        this.potriviriPestePrag = potriviriPestePrag;
        this.notificariPublicate = notificariPublicate;
        this.finalizatLa = Instant.now();
    }

    public void esec(String codEroare) {
        this.stare = StareExecutieAnalitica.ESUATA;
        this.codEroare = codEroare;
        this.finalizatLa = Instant.now();
    }

    public UUID getId() { return id; }
    public StareExecutieAnalitica getStare() { return stare; }
    public long getPerechiEvaluate() { return perechiEvaluate; }
    public long getPotriviriPestePrag() { return potriviriPestePrag; }
    public long getNotificariPublicate() { return notificariPublicate; }
    public String getCodEroare() { return codEroare; }
    public Instant getPornitLa() { return pornitLa; }
    public Instant getFinalizatLa() { return finalizatLa; }
}
