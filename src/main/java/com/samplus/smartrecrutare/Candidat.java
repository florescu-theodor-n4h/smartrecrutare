package com.samplus.smartrecrutare;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/***
 * Clasa candidat. Reprezinta entitatea Spring pentru candidatul nostru.
 */
@Entity
// @Table(schema = "recrutare")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// nu functioneaza cand id inca nu este salvat @EqualsAndHashCode
@Schema(description = "Candidat din sistemul de recrutare")
public class Candidat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID unic al candidatului", example = "1")
    // Id-ul candidatului.
    private Long id;
    // Numele si prenumele
    @Schema(description = "Nume și prenume complet", example = "Ion Popescu")
    private String numePrenume;
    @Column(unique = true, nullable = false)
    @Schema(description = "Adresa de email a candidatului", example = "ion.popescu@email.com")
    // Adresa Mail.
    private String mail;
    // Adresa telefon.
    @Schema(description = "Număr de telefon", example = "+40722111222")
    private String tel;

    /**
     * Se verifica daca doi Candidati sunt aceeasi entitate.
     * Doi candidati null nu sunt niciodata identici.
     * @param obj obiectul cu care este comparat.
     * @return true daca sunt aceeasi
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Candidat other = (Candidat) obj;

        // unregistered entities are never equal
        if (this.id == null || other.id == null) {
            return false;
        }

        // registered entities compare by database identity
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
