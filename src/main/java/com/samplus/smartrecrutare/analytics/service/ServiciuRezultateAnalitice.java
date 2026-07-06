package com.samplus.smartrecrutare.analytics.service;

import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.RezultatPotrivireResponse;
import com.samplus.smartrecrutare.models.StarePotrivire;
import com.samplus.smartrecrutare.models.TablouAdministrareAnaliticeResponse;
import org.springframework.data.domain.Pageable;

/** Contract pentru interogarile paginii de administrare. */
public interface ServiciuRezultateAnalitice {
    PaginaModel<RezultatPotrivireResponse> listare(StarePotrivire stare, Pageable pageable);

    TablouAdministrareAnaliticeResponse tablouAdministrare();
}
