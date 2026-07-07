package com.samplus.smartrecrutare;

import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.security.AuditableEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "joburi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entitatea Job - reprezinta un post de munca disponibil")
public class Job extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificator unic generat automat", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Titlul postului", example = "Senior Java Developer")
    private String titlu;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descrierea completa a postului")
    private String descriere;

    @Column(nullable = false)
    @Schema(description = "Compania afisata pentru compatibilitate cu API-ul existent", example = "Samplus SRL")
    private String companie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employer_id",
            foreignKey = @ForeignKey(name = "fk_job_employer")
    )
    @Schema(description = "Angajatorul care detine jobul", accessMode = Schema.AccessMode.READ_ONLY)
    private Employer employer;

    @Column
    @Schema(description = "Locatia jobului (oras sau remote)", example = "Bucuresti / Remote")
    private String locatie;

    @Column
    @Schema(description = "Salariul oferit (optional)", example = "5000-7000 EUR")
    private String salariu;

    @Column(nullable = false)
    @Schema(description = "Tipul contractului", example = "Full-time",
            allowableValues = {"Full-time", "Part-time", "Contract", "Internship"})
    private String tipContract;

    @Column(nullable = false)
    @Schema(description = "Starea jobului (activ/inactiv)", example = "true")
    private boolean activ = true;

    @Override
    public String toString() {
        return "Job{id=" + id + ", titlu='" + titlu + "', companie='" + companie + "', activ=" + activ + "}";
    }
}
