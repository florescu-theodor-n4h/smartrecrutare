package com.samplus.smartrecrutare.analytics.matching;

import lombok.Value;

import java.util.List;

/** Lot imutabil citit intr-o tranzactie scurta. */
@Value
public class LotDatePotrivire {
    List<DateProfilPotrivire> profiluri;
    List<DateJobPotrivire> joburi;
    List<DateTiparPotrivire> tipare;
}
