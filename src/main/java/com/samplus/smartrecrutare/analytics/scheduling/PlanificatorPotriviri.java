package com.samplus.smartrecrutare.analytics.scheduling;

import com.samplus.smartrecrutare.analytics.exception.ConflictAnaliticException;
import com.samplus.smartrecrutare.analytics.service.ServiciuExecutiiAnalitice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Porneste periodic recalcularea, fara a suprapune doua executii.
 * Proprietatile disponibile sunt {@code analytics.matching.scheduler-enabled},
 * {@code analytics.matching.initial-delay} si {@code analytics.matching.fixed-delay}.
 */
@Component
@ConditionalOnProperty(
        prefix = "analytics.matching",
        name = "scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PlanificatorPotriviri {

    private static final Logger log = LoggerFactory.getLogger(PlanificatorPotriviri.class);
    private final ServiciuExecutiiAnalitice serviciuExecutii;

    public PlanificatorPotriviri(ServiciuExecutiiAnalitice serviciuExecutii) {
        this.serviciuExecutii = serviciuExecutii;
    }

    @Scheduled(
            initialDelayString = "${analytics.matching.initial-delay:PT1M}",
            fixedDelayString = "${analytics.matching.fixed-delay:PT15M}"
    )
    public void pornestePeriodic() {
        try {
            serviciuExecutii.solicitaExecutie();
        } catch (ConflictAnaliticException exception) {
            log.debug("Executia periodica este omisa deoarece alta executie este activa");
        }
    }
}
