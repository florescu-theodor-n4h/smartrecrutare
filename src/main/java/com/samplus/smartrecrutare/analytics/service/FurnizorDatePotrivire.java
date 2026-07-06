package com.samplus.smartrecrutare.analytics.service;

import com.samplus.smartrecrutare.analytics.matching.LotDatePotrivire;

/** Citeste datele necesare procesului fara a expune entitati detasate. */
public interface FurnizorDatePotrivire {
    LotDatePotrivire citeste();
}
