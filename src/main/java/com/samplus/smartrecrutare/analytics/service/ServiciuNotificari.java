package com.samplus.smartrecrutare.analytics.service;

import com.samplus.smartrecrutare.models.MarcareNotificareCititaRequest;
import com.samplus.smartrecrutare.models.NotificareResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.PublicareNotificareRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/** Contractul API pentru mesajele localizabile. */
public interface ServiciuNotificari {
    PaginaModel<NotificareResponse> listarePentru(String destinatar, Pageable pageable);

    PaginaModel<NotificareResponse> listareAdministrativa(Pageable pageable);

    NotificareResponse publicare(PublicareNotificareRequest request);

    NotificareResponse marcheazaCitita(
            String destinatar,
            UUID notificareId,
            MarcareNotificareCititaRequest request
    );
}
