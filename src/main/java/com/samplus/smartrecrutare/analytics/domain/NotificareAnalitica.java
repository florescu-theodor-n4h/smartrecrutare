package com.samplus.smartrecrutare.analytics.domain;

import com.samplus.smartrecrutare.models.StareNotificare;
import com.samplus.smartrecrutare.security.AuditableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Mesaj persistent localizabil de catre frontend. */
@Entity
@Table(
        name = "analytics_notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_analytics_notification_result_message",
                columnNames = {"match_result_id", "message_id"}
        ),
        indexes = {
                @Index(name = "idx_analytics_notification_recipient_state", columnList = "recipient,state"),
                @Index(name = "idx_analytics_notification_created", columnList = "created_at")
        }
)
public class NotificareAnalitica extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "recipient", nullable = false, length = 320)
    private String destinatar;

    @Column(name = "message_id", nullable = false, length = 160)
    private String mesajId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "analytics_notification_placeholders",
            joinColumns = @JoinColumn(
                    name = "notification_id",
                    foreignKey = @ForeignKey(name = "fk_analytics_placeholder_notification")
            )
    )
    @MapKeyColumn(name = "placeholder_name", length = 80)
    @Column(name = "placeholder_value", nullable = false, length = 500)
    @BatchSize(size = 50)
    private Map<String, String> substituenti = new LinkedHashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private StareNotificare stare;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_result_id", foreignKey = @ForeignKey(name = "fk_analytics_notification_match"))
    private RezultatPotrivire rezultatPotrivire;

    @Column(name = "read_at")
    private Instant cititLa;

    protected NotificareAnalitica() {
        // Constructor necesar pentru JPA.
    }

    public static NotificareAnalitica creare(
            String destinatar,
            String mesajId,
            Map<String, String> substituenti,
            RezultatPotrivire rezultatPotrivire
    ) {
        NotificareAnalitica notificare = new NotificareAnalitica();
        notificare.destinatar = destinatar;
        notificare.mesajId = mesajId;
        notificare.substituenti = new LinkedHashMap<>(substituenti);
        notificare.rezultatPotrivire = rezultatPotrivire;
        notificare.stare = StareNotificare.NOUA;
        return notificare;
    }

    public void marcheazaCitita() {
        if (stare != StareNotificare.CITITA) {
            this.stare = StareNotificare.CITITA;
            this.cititLa = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public String getDestinatar() { return destinatar; }
    public String getMesajId() { return mesajId; }
    public Map<String, String> getSubstituenti() { return Map.copyOf(substituenti); }
    public StareNotificare getStare() { return stare; }
    public RezultatPotrivire getRezultatPotrivire() { return rezultatPotrivire; }
    public Instant getCititLa() { return cititLa; }
}
