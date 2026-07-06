package com.samplus.smartrecrutare.analytics.mapper;

import com.samplus.smartrecrutare.analytics.domain.ExecutieAnalitica;
import com.samplus.smartrecrutare.analytics.domain.NotificareAnalitica;
import com.samplus.smartrecrutare.analytics.domain.ProfilAnaliticCandidat;
import com.samplus.smartrecrutare.analytics.domain.RezultatPotrivire;
import com.samplus.smartrecrutare.analytics.domain.TiparPotrivire;
import com.samplus.smartrecrutare.models.ExecutieAnaliticaResponse;
import com.samplus.smartrecrutare.models.NotificareResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.ProfilCandidatResponse;
import com.samplus.smartrecrutare.models.RezultatPotrivireResponse;
import com.samplus.smartrecrutare.models.ScorDetaliatResponse;
import com.samplus.smartrecrutare.models.TiparPotrivireResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/** Converteaza entitatile interne in contracte API imutabile. */
@Component
public class MapperAnalitice {

    public ProfilCandidatResponse profil(ProfilAnaliticCandidat profil) {
        return new ProfilCandidatResponse(
                profil.getId(),
                profil.getCandidat().getId(),
                profil.getCandidat().getNumePrenume(),
                profil.getAbilitati(),
                profil.getLocatiiPreferate(),
                profil.getTipContractPreferat(),
                profil.getCuvinteCheie(),
                profil.getCreatedAt(),
                profil.getCreatedBy(),
                profil.getUpdatedAt(),
                profil.getUpdatedBy(),
                profil.getVersion()
        );
    }

    public TiparPotrivireResponse tipar(TiparPotrivire tipar) {
        return new TiparPotrivireResponse(
                tipar.getId(),
                tipar.getNume(),
                tipar.getDescriere(),
                tipar.getPondereAbilitati(),
                tipar.getPondereLocatie(),
                tipar.getPondereContract(),
                tipar.getPondereCuvinteCheie(),
                tipar.getPragNotificare(),
                tipar.isActiv(),
                tipar.getCreatedAt(),
                tipar.getCreatedBy(),
                tipar.getUpdatedAt(),
                tipar.getUpdatedBy(),
                tipar.getVersion()
        );
    }

    public ExecutieAnaliticaResponse executie(ExecutieAnalitica executie) {
        return new ExecutieAnaliticaResponse(
                executie.getId(),
                executie.getStare(),
                executie.getPerechiEvaluate(),
                executie.getPotriviriPestePrag(),
                executie.getNotificariPublicate(),
                executie.getCodEroare(),
                executie.getPornitLa(),
                executie.getFinalizatLa(),
                executie.getCreatedAt(),
                executie.getCreatedBy(),
                executie.getVersion()
        );
    }

    public RezultatPotrivireResponse rezultat(RezultatPotrivire rezultat) {
        return new RezultatPotrivireResponse(
                rezultat.getId(),
                rezultat.getCandidat().getId(),
                rezultat.getCandidat().getNumePrenume(),
                rezultat.getJob().getId(),
                rezultat.getJob().getTitlu(),
                rezultat.getJob().getCompanie(),
                rezultat.getTipar().getId(),
                rezultat.getTipar().getNume(),
                rezultat.getScorTotal(),
                new ScorDetaliatResponse(
                        rezultat.getScorAbilitati(),
                        rezultat.getScorLocatie(),
                        rezultat.getScorContract(),
                        rezultat.getScorCuvinteCheie()
                ),
                rezultat.getStare(),
                rezultat.getEvaluatLa(),
                rezultat.getVersion()
        );
    }

    public NotificareResponse notificare(NotificareAnalitica notificare) {
        return new NotificareResponse(
                notificare.getId(),
                notificare.getDestinatar(),
                notificare.getMesajId(),
                notificare.getSubstituenti(),
                notificare.getStare(),
                notificare.getRezultatPotrivire() == null ? null : notificare.getRezultatPotrivire().getId(),
                notificare.getCreatedAt(),
                notificare.getCititLa(),
                notificare.getVersion()
        );
    }

    public <T> PaginaModel<T> pagina(Page<?> pagina, List<T> continut) {
        return new PaginaModel<>(
                List.copyOf(continut),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }
}
