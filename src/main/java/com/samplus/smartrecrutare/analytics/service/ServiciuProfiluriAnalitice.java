package com.samplus.smartrecrutare.analytics.service;

import com.samplus.smartrecrutare.models.ActualizareProfilCandidatRequest;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.ProfilCandidatRequest;
import com.samplus.smartrecrutare.models.ProfilCandidatResponse;
import org.springframework.data.domain.Pageable;

/** Contract pentru administrarea profilurilor candidatilor. */
public interface ServiciuProfiluriAnalitice {
    ProfilCandidatResponse creare(Long candidatId, ProfilCandidatRequest request);

    ProfilCandidatResponse gasire(Long candidatId);

    PaginaModel<ProfilCandidatResponse> listare(Pageable pageable);

    ProfilCandidatResponse inlocuire(Long candidatId, ActualizareProfilCandidatRequest request);

    void stergere(Long candidatId, Long versiune);
}
