package com.samplus.smartrecrutare.analytics.matching;

import com.samplus.smartrecrutare.models.StarePotrivire;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica algoritmul pur fara acces la baza de date. */
class StrategiePotrivirePonderataTest {

    private final StrategiePotrivirePonderata strategie = new StrategiePotrivirePonderata();

    @Test
    void calculeazaComponenteleSiScorulPonderat() {
        var profil = new DateProfilPotrivire(
                7L,
                "Ion Popescu",
                "ion@example.com",
                Set.of("java", "spring"),
                Set.of("bucuresti"),
                "full-time",
                Set.of("microservices", "docker")
        );
        var job = new DateJobPotrivire(
                9L,
                "Senior Java Developer",
                "Spring Boot si microservices",
                "Samplus",
                "Bucuresti / Remote",
                "Full-time"
        );
        var tipar = new DateTiparPotrivire(
                UUID.randomUUID(),
                "Standard",
                50,
                20,
                10,
                20,
                80
        );

        ScorPotrivire scor = strategie.calculeaza(profil, job, tipar);

        assertThat(scor.getAbilitati()).isEqualTo(100);
        assertThat(scor.getLocatie()).isEqualTo(100);
        assertThat(scor.getContract()).isEqualTo(100);
        assertThat(scor.getCuvinteCheie()).isEqualTo(50);
        assertThat(scor.getTotal()).isEqualTo(90);
        assertThat(scor.getStare()).isEqualTo(StarePotrivire.PESTE_PRAG);
    }
}
