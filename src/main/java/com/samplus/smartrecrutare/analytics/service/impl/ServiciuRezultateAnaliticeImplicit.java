package com.samplus.smartrecrutare.analytics.service.impl;

import com.samplus.smartrecrutare.DepozitJoburi;
import com.samplus.smartrecrutare.analytics.repository.ExecutieAnaliticaRepository;
import com.samplus.smartrecrutare.analytics.repository.NotificareAnaliticaRepository;
import com.samplus.smartrecrutare.analytics.repository.ProfilAnaliticCandidatRepository;
import com.samplus.smartrecrutare.analytics.repository.RezultatPotrivireRepository;
import com.samplus.smartrecrutare.analytics.repository.TiparPotrivireRepository;
import com.samplus.smartrecrutare.analytics.mapper.MapperAnalitice;
import com.samplus.smartrecrutare.analytics.service.ServiciuRezultateAnalitice;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.RezultatPotrivireResponse;
import com.samplus.smartrecrutare.models.StareExecutieAnalitica;
import com.samplus.smartrecrutare.models.StareNotificare;
import com.samplus.smartrecrutare.models.StarePotrivire;
import com.samplus.smartrecrutare.models.TablouAdministrareAnaliticeResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Agrega interogarile necesare paginii administratorului. */
@Service
public class ServiciuRezultateAnaliticeImplicit implements ServiciuRezultateAnalitice {

    private static final List<StareExecutieAnalitica> STARI_ACTIVE = List.of(
            StareExecutieAnalitica.IN_ASTEPTARE,
            StareExecutieAnalitica.IN_EXECUTIE
    );

    private final RezultatPotrivireRepository rezultatRepository;
    private final ProfilAnaliticCandidatRepository profilRepository;
    private final TiparPotrivireRepository tiparRepository;
    private final NotificareAnaliticaRepository notificareRepository;
    private final ExecutieAnaliticaRepository executieRepository;
    private final DepozitJoburi depozitJoburi;
    private final MapperAnalitice mapper;

    public ServiciuRezultateAnaliticeImplicit(
            RezultatPotrivireRepository rezultatRepository,
            ProfilAnaliticCandidatRepository profilRepository,
            TiparPotrivireRepository tiparRepository,
            NotificareAnaliticaRepository notificareRepository,
            ExecutieAnaliticaRepository executieRepository,
            DepozitJoburi depozitJoburi,
            MapperAnalitice mapper
    ) {
        this.rezultatRepository = rezultatRepository;
        this.profilRepository = profilRepository;
        this.tiparRepository = tiparRepository;
        this.notificareRepository = notificareRepository;
        this.executieRepository = executieRepository;
        this.depozitJoburi = depozitJoburi;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaModel<RezultatPotrivireResponse> listare(StarePotrivire stare, Pageable pageable) {
        var pagina = stare == null
                ? rezultatRepository.findAllByOrderByScorTotalDesc(pageable)
                : rezultatRepository.findByStareOrderByScorTotalDesc(stare, pageable);
        return mapper.pagina(pagina, pagina.getContent().stream().map(mapper::rezultat).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TablouAdministrareAnaliticeResponse tablouAdministrare() {
        return new TablouAdministrareAnaliticeResponse(
                profilRepository.count(),
                depozitJoburi.countByActivTrue(),
                tiparRepository.countByActivTrue(),
                rezultatRepository.countByStare(StarePotrivire.PESTE_PRAG),
                notificareRepository.countByStare(StareNotificare.NOUA),
                executieRepository.existsByStareIn(STARI_ACTIVE)
        );
    }
}
