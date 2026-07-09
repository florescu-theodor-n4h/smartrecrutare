package com.samplus.smartrecrutare;

import com.samplus.smartrecrutare.auth.Auth0OAuthException;
import com.samplus.smartrecrutare.employer.exception.DuplicateFiscalCodeException;
import com.samplus.smartrecrutare.employer.exception.EmployerInUseException;
import com.samplus.smartrecrutare.employer.exception.EmployerNotFoundException;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthBadCredentialsException;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthConflictException;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthDisabledException;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;

@RestControllerAdvice
public class HandlerExceptiiGlobal {
    private static final Logger log = LoggerFactory.getLogger(HandlerExceptiiGlobal.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    ProblemDetail handleNoHandlerFound(NoHandlerFoundException exception, HttpServletRequest request) {
        log.warn("Unknown request: {} {}", request.getMethod(), request.getRequestURI());
        return problema(HttpStatus.NOT_FOUND, "Ruta inexistenta", "Endpointul solicitat nu exista");
    }

    @ExceptionHandler({EmployerNotFoundException.class, LocalAuthNotFoundException.class, EntityNotFoundException.class})
    ProblemDetail notFound(RuntimeException exception) {
        return problema(HttpStatus.NOT_FOUND, "Resursa inexistenta", exception.getMessage());
    }

    @ExceptionHandler({
            DuplicateFiscalCodeException.class,
            LocalAuthConflictException.class,
            EmployerInUseException.class,
            DataIntegrityViolationException.class
    })
    ProblemDetail conflict(RuntimeException exception) {
        return problema(HttpStatus.CONFLICT, "Conflict de date", exception.getMessage());
    }

    @ExceptionHandler(LocalAuthBadCredentialsException.class)
    ProblemDetail badLocalCredentials(LocalAuthBadCredentialsException exception) {
        return problema(HttpStatus.UNAUTHORIZED, "Credentiale invalide", exception.getMessage());
    }

    @ExceptionHandler(LocalAuthDisabledException.class)
    ProblemDetail localAuthDisabled(LocalAuthDisabledException exception) {
        return problema(HttpStatus.SERVICE_UNAVAILABLE, "LocalAuth indisponibil", exception.getMessage());
    }

    @ExceptionHandler(Auth0OAuthException.class)
    ProblemDetail auth0OAuth(Auth0OAuthException exception) {
        log.warn("Auth0 OAuth failure: {}", exception.getMessage());
        ProblemDetail detail = problema(exception.getResponseStatus(), "Eroare Auth0", exception.getMessage());

        if (exception.getAuth0Status() != null) {
            detail.setProperty("auth0Status", exception.getAuth0Status());
        }

        if (exception.getAuth0Body() != null && !exception.getAuth0Body().isBlank()) {
            detail.setProperty("auth0Response", exception.getAuth0Body());
        }

        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalidBusinessRequest(IllegalArgumentException exception) {
        return problema(HttpStatus.BAD_REQUEST, "Cerere invalida", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidBody(MethodArgumentNotValidException exception) {
        ProblemDetail detail = problema(
                HttpStatus.BAD_REQUEST,
                "Validare esuata",
                "Unul sau mai multe campuri sunt invalide"
        );
        var errors = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    ProblemDetail invalidParameter(RuntimeException exception) {
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
