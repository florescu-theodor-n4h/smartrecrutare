package com.samplus.smartrecrutare.analytics.matching;

import java.util.List;

/** Lot imutabil citit intr-o tranzactie scurta. */
public record LotDatePotrivire(
        List<DateProfilPotrivire> profiluri,
        List<DateJobPotrivire> joburi,
        List<DateTiparPotrivire> tipare
) {
}
