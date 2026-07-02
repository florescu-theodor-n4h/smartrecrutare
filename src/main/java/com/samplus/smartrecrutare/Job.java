package com.samplus.smartrecrutare;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entitatea JPA care reprezintă un post de muncă (job) publicat în sistem.
 *
 * <p>Câmpurile de audit ({@code creatLa}, {@code actualizatLa}) sunt gestionate
 * automat de JPA lifecycle callbacks — nu trebuie setate manual din serviciu.</p>
 */
@Entity
@Table(name = "joburi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entitatea Job — reprezintă un post de muncă disponibil")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificator unic generat automat", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Titlul postului", example = "Senior Java Developer")
    private String titlu;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descrierea completă a postului")
    private String descriere;

    @Column(nullable = false)
    @Schema(description = "Compania care publică jobul", example = "Samplus SRL")
    private String companie;

    @Column
    @Schema(description = "Locația jobului (oraș sau remote)", example = "București / Remote")
    private String locatie;

    @Column
    @Schema(description = "Salariul oferit (opțional)", example = "5000-7000 EUR")
    private String salariu;

    @Column(nullable = false)
    @Schema(description = "Tipul contractului", example = "Full-time",
            allowableValues = {"Full-time", "Part-time", "Contract", "Internship"})
    private String tipContract;

    @Column(nullable = false)
    @Schema(description = "Starea jobului (activ/inactiv)", example = "true")
    private boolean activ = true;

    @Column(nullable = false, updatable = false)
    @Schema(description = "Momentul creării înregistrării", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant creatLa;

    @Column(nullable = false)
    @Schema(description = "Momentul ultimei actualizări", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant actualizatLa;

    // -------------------------------------------------------------------------
    // Lifecycle callbacks — audit automat
    // -------------------------------------------------------------------------

    @PrePersist
    protected void laCreare() {
        this.creatLa = Instant.now();
        this.actualizatLa = Instant.now();
    }

    @PreUpdate
    protected void laActualizare() {
        this.actualizatLa = Instant.now();
    }

    // -------------------------------------------------------------------------
    // Constructori
    // -------------------------------------------------------------------------

    // LOMBOK

    // -------------------------------------------------------------------------
    // Getteri & Setteri
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Job{id=" + id + ", titlu='" + titlu + "', companie='" + companie + "', activ=" + activ + "}";
    }
}