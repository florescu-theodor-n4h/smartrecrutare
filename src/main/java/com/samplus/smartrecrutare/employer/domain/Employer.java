package com.samplus.smartrecrutare.employer.domain;

import com.samplus.smartrecrutare.security.AuditableEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "employers",
        indexes = {
                @Index(name = "idx_employer_cod_fiscal", columnList = "cod_fiscal", unique = true),
                @Index(name = "idx_employer_status", columnList = "status")
        }
)
@Schema(description = "Angajator care publica joburi in SmartRecrutare")
public class Employer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificator unic generat automat", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 150)
    @Schema(description = "Numele public al angajatorului", example = "Samplus")
    private String nume;

    @Column(name = "denumire_legala", nullable = false, length = 200)
    @Schema(description = "Denumirea legala a companiei", example = "Samplus SRL")
    private String denumireLegala;

    @Column(name = "cod_fiscal", nullable = false, unique = true, length = 40)
    @Schema(description = "Cod fiscal unic", example = "RO12345678")
    private String codFiscal;

    @Column(name = "email_contact", length = 180)
    @Schema(description = "Email de contact", example = "contact@samplus.ro")
    private String emailContact;

    @Column(name = "telefon_contact", length = 40)
    @Schema(description = "Telefon de contact", example = "+40722111222")
    private String telefonContact;

    @Column(length = 250)
    @Schema(description = "Website companie", example = "https://samplus.ro")
    private String website;

    @Column(length = 150)
    @Schema(description = "Locatia principala", example = "Bucuresti")
    private String locatie;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descriere scurta a angajatorului")
    private String descriere;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Schema(description = "Statusul angajatorului")
    private EmployerStatus status = EmployerStatus.IN_VERIFICARE;

    protected Employer() {
        // Constructor necesar pentru JPA.
    }

    public static Employer creare(
            String nume,
            String denumireLegala,
            String codFiscal,
            String emailContact,
            String telefonContact,
            String website,
            String locatie,
            String descriere,
            EmployerStatus status
    ) {
        Employer employer = new Employer();
        employer.inlocuire(
                nume,
                denumireLegala,
                codFiscal,
                emailContact,
                telefonContact,
                website,
                locatie,
                descriere,
                status
        );
        return employer;
    }

    public void inlocuire(
            String nume,
            String denumireLegala,
            String codFiscal,
            String emailContact,
            String telefonContact,
            String website,
            String locatie,
            String descriere,
            EmployerStatus status
    ) {
        this.nume = nume;
        this.denumireLegala = denumireLegala;
        this.codFiscal = codFiscal;
        this.emailContact = emailContact;
        this.telefonContact = telefonContact;
        this.website = website;
        this.locatie = locatie;
        this.descriere = descriere;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getNume() { return nume; }
    public String getDenumireLegala() { return denumireLegala; }
    public String getCodFiscal() { return codFiscal; }
    public String getEmailContact() { return emailContact; }
    public String getTelefonContact() { return telefonContact; }
    public String getWebsite() { return website; }
    public String getLocatie() { return locatie; }
    public String getDescriere() { return descriere; }
    public EmployerStatus getStatus() { return status; }
}
