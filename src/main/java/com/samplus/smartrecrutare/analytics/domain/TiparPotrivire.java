package com.samplus.smartrecrutare.analytics.domain;

import com.samplus.smartrecrutare.security.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

/** Tipar administrabil pentru calcularea unui scor ponderat. */
@Entity
@Table(
        name = "analytics_match_patterns",
        indexes = {
                @Index(name = "idx_analytics_pattern_name", columnList = "name", unique = true),
                @Index(name = "idx_analytics_pattern_active", columnList = "active")
        }
)
public class TiparPotrivire extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150, unique = true)
    private String nume;

    @Column(length = 500)
    private String descriere;

    @Column(name = "skill_weight", nullable = false)
    private int pondereAbilitati;

    @Column(name = "location_weight", nullable = false)
    private int pondereLocatie;

    @Column(name = "contract_weight", nullable = false)
    private int pondereContract;

    @Column(name = "keyword_weight", nullable = false)
    private int pondereCuvinteCheie;

    @Column(name = "notification_threshold", nullable = false)
    private int pragNotificare;

    @Column(name = "active", nullable = false)
    private boolean activ;

    protected TiparPotrivire() {
        // Constructor necesar pentru JPA.
    }

    public static TiparPotrivire creare(
            String nume,
            String descriere,
            int pondereAbilitati,
            int pondereLocatie,
            int pondereContract,
            int pondereCuvinteCheie,
            int pragNotificare,
            boolean activ
    ) {
        TiparPotrivire tipar = new TiparPotrivire();
        tipar.inlocuire(
                nume,
                descriere,
                pondereAbilitati,
                pondereLocatie,
                pondereContract,
                pondereCuvinteCheie,
                pragNotificare,
                activ
        );
        return tipar;
    }

    public void inlocuire(
            String nume,
            String descriere,
            int pondereAbilitati,
            int pondereLocatie,
            int pondereContract,
            int pondereCuvinteCheie,
            int pragNotificare,
            boolean activ
    ) {
        this.nume = nume;
        this.descriere = descriere;
        this.pondereAbilitati = pondereAbilitati;
        this.pondereLocatie = pondereLocatie;
        this.pondereContract = pondereContract;
        this.pondereCuvinteCheie = pondereCuvinteCheie;
        this.pragNotificare = pragNotificare;
        this.activ = activ;
    }

    public UUID getId() { return id; }
    public String getNume() { return nume; }
    public String getDescriere() { return descriere; }
    public int getPondereAbilitati() { return pondereAbilitati; }
    public int getPondereLocatie() { return pondereLocatie; }
    public int getPondereContract() { return pondereContract; }
    public int getPondereCuvinteCheie() { return pondereCuvinteCheie; }
    public int getPragNotificare() { return pragNotificare; }
    public boolean isActiv() { return activ; }
}
