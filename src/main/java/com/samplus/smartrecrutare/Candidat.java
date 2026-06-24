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
public class Candidat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID unic al candidatului", example = "1")
    /// Id-ul candidatului.
    private Long id;
    /// Numele si prenumele
    @Schema(description = "Nume și prenume complet", example = "Ion Popescu")
    private String numePrenume;
    @Column(unique = true, nullable = false)
    @Schema(description = "Adresa de email a candidatului", example = "ion.popescu@email.com")
    /// Adresa Mail.
    private String mail;
    /// Adresa telefon.
    @Schema(description = "Număr de telefon", example = "+40722111222")
    private String tel;
}