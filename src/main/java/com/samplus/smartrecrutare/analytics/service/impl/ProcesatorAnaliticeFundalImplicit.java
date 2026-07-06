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
import com.samplus.smartrecrutare.analytics.service.ProcesatorAnaliticeFundal;
import com.samplus.smartrecrutare.analytics.service.RegistruExecutiiAnalitice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Ruleaza matricea de potrivire pe executorul dedicat. */
@Service
public class ProcesatorAnaliticeFundalImplicit implements ProcesatorAnaliticeFundal {

    private static final Logger log = LoggerFactory.getLogger(ProcesatorAnaliticeFundalImplicit.class);
    private static final String COD_EROARE = "ANALYTICS_EXECUTION_FAILED";

    private final FurnizorDatePotrivire furnizorDate;
    private final StrategiePotrivire strategie;
    private final PersistentaRezultatPotrivire persistenta;
    private final RegistruExecutiiAnalitice registru;

    public ProcesatorAnaliticeFundalImplicit(
            FurnizorDatePotrivire furnizorDate,
            StrategiePotrivire strategie,
            PersistentaRezultatPotrivire persistenta,
            RegistruExecutiiAnalitice registru
    ) {
        this.furnizorDate = furnizorDate;
        this.strategie = strategie;
        this.persistenta = persistenta;
        this.registru = registru;
    }

    @Override
    @Async("analyticsTaskExecutor")
    public CompletableFuture<Void> proceseaza(UUID executieId) {
        try {
            registru.marcheazaPornita(executieId);
            LotDatePotrivire lot = furnizorDate.citeste();
            long evaluate = 0;
            long pestePrag = 0;
            long notificari = 0;

            for (DateTiparPotrivire tipar : lot.getTipare()) {
                for (DateProfilPotrivire profil : lot.getProfiluri()) {
                    for (DateJobPotrivire job : lot.getJoburi()) {
                        ScorPotrivire scor = strategie.calculeaza(profil, job, tipar);
                        RezultatPersistare rezultat = persistenta.salveaza(scor);
                        evaluate++;
                        if (rezultat.isPestePrag()) {
                            pestePrag++;
                        }
                        if (rezultat.isNotificarePublicata()) {
                            notificari++;
                        }
                    }
                }
            }
            registru.marcheazaFinalizata(executieId, evaluate, pestePrag, notificari);
        } catch (RuntimeException exception) {
            log.error("Executia analitica {} a esuat", executieId, exception);
            marcheazaEsuataFaraMascare(executieId);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void marcheazaEsuataFaraMascare(UUID executieId) {
        try {
            registru.marcheazaEsuata(executieId, COD_EROARE);
        } catch (RuntimeException exceptieRegistru) {
            log.error("Starea de esec nu a putut fi salvata pentru executia {}", executieId, exceptieRegistru);
        }
    }
}
