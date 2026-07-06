package com.samplus.smartrecrutare.analytics.service.impl;

import com.samplus.smartrecrutare.analytics.domain.NotificareAnalitica;
import com.samplus.smartrecrutare.analytics.exception.ResursaAnaliticaNegasitaException;
import com.samplus.smartrecrutare.analytics.mapper.MapperAnalitice;
import com.samplus.smartrecrutare.analytics.repository.NotificareAnaliticaRepository;
import com.samplus.smartrecrutare.analytics.service.ServiciuNotificari;
import com.samplus.smartrecrutare.analytics.service.ValidatorVersiuneAnalitica;
import com.samplus.smartrecrutare.models.MarcareNotificareCititaRequest;
import com.samplus.smartrecrutare.models.NotificareResponse;
import com.samplus.smartrecrutare.models.PaginaModel;
import com.samplus.smartrecrutare.models.PublicareNotificareRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Implementare persistenta pentru centrul de notificari. */
@Service
public class ServiciuNotificariImplicit implements ServiciuNotificari {

    private final NotificareAnaliticaRepository notificareRepository;
    private final ValidatorVersiuneAnalitica validatorVersiune;
    private final MapperAnalitice mapper;

    public ServiciuNotificariImplicit(
            NotificareAnaliticaRepository notificareRepository,
            ValidatorVersiuneAnalitica validatorVersiune,
            MapperAnalitice mapper
    ) {
        this.notificareRepository = notificareRepository;
        this.validatorVersiune = validatorVersiune;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaModel<NotificareResponse> listarePentru(String destinatar, Pageable pageable) {
        var pagina = notificareRepository.findByDestinatarIgnoreCaseOrderByCreatedAtDesc(
                destinatar,
                pageable
        );
        return mapper.pagina(
                pagina,
                pagina.getContent().stream().map(mapper::notificare).collect(Collectors.toList())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaModel<NotificareResponse> listareAdministrativa(Pageable pageable) {
        var pagina = notificareRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapper.pagina(
                pagina,
                pagina.getContent().stream().map(mapper::notificare).collect(Collectors.toList())
        );
    }

    @Override
    @Transactional
    public NotificareResponse publicare(PublicareNotificareRequest request) {
        NotificareAnalitica notificare = NotificareAnalitica.creare(
                request.getDestinatar().trim().toLowerCase(java.util.Locale.ROOT),
                request.getMesajId().trim(),
                request.getSubstituenti() == null ? Map.of() : request.getSubstituenti(),
                null
        );
        notificareRepository.saveAndFlush(notificare);
        return mapper.notificare(notificare);
    }

    @Override
    @Transactional
    public NotificareResponse marcheazaCitita(
            String destinatar,
            UUID notificareId,
            MarcareNotificareCititaRequest request
    ) {
        NotificareAnalitica notificare = notificareRepository
                .findByIdAndDestinatarIgnoreCase(notificareId, destinatar)
                .orElseThrow(() -> new ResursaAnaliticaNegasitaException(
                        "Notificarea " + notificareId + " nu exista pentru utilizator"
                ));
        validatorVersiune.verifica(
                "Notificarea",
                notificareId,
                notificare.getVersion(),
                request.getVersiune()
        );
        notificare.marcheazaCitita();
        notificareRepository.flush();
        return mapper.notificare(notificare);
    }
}
