package com.samplus.smartrecrutare.analytics.service.impl;

import com.samplus.smartrecrutare.analytics.matching.DateJobPotrivire;
import com.samplus.smartrecrutare.analytics.matching.DateProfilPotrivire;
import com.samplus.smartrecrutare.analytics.matching.DateTiparPotrivire;
import com.samplus.smartrecrutare.analytics.matching.LotDatePotrivire;
import com.samplus.smartrecrutare.analytics.matching.RezultatPersistare;
import com.samplus.smartrecrutare.analytics.matching.ScorPotrivire;
import com.samplus.smartrecrutare.analytics.matching.StrategiePotrivire;
import com.samplus.smartrecrutare.analytics.service.FurnizorDatePotrivire;
import com.samplus.smartrecrutare.analytics.service.PersistentaRezultatPotrivire;
import com.samplus.smartrecrutare.analytics.service.RegistruExecutiiAnalitice;
import com.samplus.smartrecrutare.models.StarePotrivire;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifica orchestrarea asincrona fara acces la baza de date. */
class ProcesatorAnaliticeFundalImplicitTest {

    @Test
    void proceseazaMatriceaSiSalveazaContorii() {
        FurnizorDatePotrivire furnizor = mock(FurnizorDatePotrivire.class);
        StrategiePotrivire strategie = mock(StrategiePotrivire.class);
        PersistentaRezultatPotrivire persistenta = mock(PersistentaRezultatPotrivire.class);
        RegistruExecutiiAnalitice registru = mock(RegistruExecutiiAnalitice.class);
        var procesator = new ProcesatorAnaliticeFundalImplicit(
                furnizor,
                strategie,
                persistenta,
                registru
        );
        UUID executieId = UUID.randomUUID();
        var profil = new DateProfilPotrivire(1L, "Candidat", "candidat@example.com", Set.of("java"), Set.of(), null, Set.of());
        var job = new DateJobPotrivire(2L, "Java", "Java", "Companie", null, null);
        var tipar = new DateTiparPotrivire(UUID.randomUUID(), "Standard", 100, 0, 0, 0, 50);
        var scor = new ScorPotrivire(
                profil,
                job,
                tipar,
                100,
                100,
                100,
                100,
                100,
                StarePotrivire.PESTE_PRAG
        );
        when(furnizor.citeste()).thenReturn(new LotDatePotrivire(
                Arrays.asList(profil),
                Arrays.asList(job),
                Arrays.asList(tipar)
        ));
        when(strategie.calculeaza(profil, job, tipar)).thenReturn(scor);
        when(persistenta.salveaza(scor)).thenReturn(new RezultatPersistare(true, true));

        procesator.proceseaza(executieId).join();

        verify(registru).marcheazaPornita(executieId);
        verify(persistenta).salveaza(scor);
        verify(registru).marcheazaFinalizata(executieId, 1, 1, 1);
    }
}
