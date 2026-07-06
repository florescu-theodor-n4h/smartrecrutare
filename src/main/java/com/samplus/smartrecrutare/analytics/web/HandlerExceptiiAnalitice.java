package com.samplus.smartrecrutare.analytics.web;

import com.samplus.smartrecrutare.analytics.exception.CerereAnaliticaInvalidaException;
import com.samplus.smartrecrutare.analytics.exception.ConflictAnaliticException;
import com.samplus.smartrecrutare.analytics.exception.ResursaAnaliticaNegasitaException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.LinkedHashMap;

/** Traduce exceptiile domeniului in raspunsuri Problem Details. */
@RestControllerAdvice(basePackages = "com.samplus.smartrecrutare.analytics.web")
public class HandlerExceptiiAnalitice {

    @ExceptionHandler(ResursaAnaliticaNegasitaException.class)
    ProblemDetail resursaNegasita(ResursaAnaliticaNegasitaException exception) {
        return problema(HttpStatus.NOT_FOUND, "Resursa analitica inexistenta", exception.getMessage());
    }

    @ExceptionHandler(ConflictAnaliticException.class)
    ProblemDetail conflict(ConflictAnaliticException exception) {
        return problema(HttpStatus.CONFLICT, "Conflict de stare", exception.getMessage());
    }

    @ExceptionHandler({OptimisticLockException.class, OptimisticLockingFailureException.class})
    ProblemDetail conflictConcurent(RuntimeException exception) {
        return problema(
                HttpStatus.CONFLICT,
                "Conflict de stare",
                "Resursa a fost modificata de alta operatie"
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail constrangereDate(DataIntegrityViolationException exception) {
        return problema(
                HttpStatus.CONFLICT,
                "Conflict de persistenta",
                "Operatia incalca o regula de integritate a datelor"
        );
    }

    @ExceptionHandler(CerereAnaliticaInvalidaException.class)
    ProblemDetail cerereInvalida(CerereAnaliticaInvalidaException exception) {
        return problema(HttpStatus.BAD_REQUEST, "Cerere analitica invalida", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail corpInvalid(MethodArgumentNotValidException exception) {
        ProblemDetail detalii = problema(
                HttpStatus.BAD_REQUEST,
                "Validare esuata",
                "Unul sau mai multe campuri sunt invalide"
        );
        var erori = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(eroare ->
                erori.putIfAbsent(eroare.getField(), eroare.getDefaultMessage())
        );
        detalii.setProperty("errors", erori);
        return detalii;
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    ProblemDetail parametruInvalid(RuntimeException exception) {
        return problema(
                HttpStatus.BAD_REQUEST,
                "Parametru invalid",
                "Unul sau mai multi parametri nu respecta limitele API"
        );
    }

    private ProblemDetail problema(HttpStatus status, String titlu, String detaliu) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detaliu);
        problema.setTitle(titlu);
        return problema;
    }
}
