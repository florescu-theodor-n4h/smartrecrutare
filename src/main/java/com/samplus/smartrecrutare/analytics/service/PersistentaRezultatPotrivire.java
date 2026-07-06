package com.samplus.smartrecrutare.analytics.service;

import com.samplus.smartrecrutare.analytics.matching.RezultatPersistare;
import com.samplus.smartrecrutare.analytics.matching.ScorPotrivire;

/** Salveaza atomic un rezultat si notificarea sa optionala. */
public interface PersistentaRezultatPotrivire {
    RezultatPersistare salveaza(ScorPotrivire scor);
}
