package com.samplus.smartrecrutare.bot.web;

import com.samplus.smartrecrutare.bot.exception.BotConflictException;
import com.samplus.smartrecrutare.bot.exception.BotResourceNotFoundException;
import com.samplus.smartrecrutare.bot.exception.BotValidationException;
import com.samplus.smartrecrutare.bot.exception.RobotClientException;
import com.samplus.smartrecrutare.bot.exception.RobotClientNotConfiguredException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;

@RestControllerAdvice(assignableTypes = GptRobotController.class)
public class BotApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BotApiExceptionHandler.class);

    @ExceptionHandler(BotResourceNotFoundException.class)
    ProblemDetail handleNotFound(BotResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Bot resource not found", exception.getMessage());
    }

    @ExceptionHandler({
            BotConflictException.class,
            OptimisticLockException.class,
            OptimisticLockingFailureException.class
    })
    ProblemDetail handleConflict(RuntimeException exception) {
        return problem(HttpStatus.CONFLICT, "Concurrent or conflicting change", exception.getMessage());
    }

    @ExceptionHandler(BotValidationException.class)
    ProblemDetail handleBusinessValidation(BotValidationException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid bot request", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleBeanValidation(MethodArgumentNotValidException exception) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Request validation failed", "One or more fields are invalid");
        var errors = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintValidation(ConstraintViolationException exception) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Request validation failed", "One or more parameters are invalid");
        var errors = new LinkedHashMap<String, String>();
        exception.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(RobotClientNotConfiguredException.class)
    ProblemDetail handleRobotNotConfigured(RobotClientNotConfiguredException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Robot API is not configured", exception.getMessage());
    }

    @ExceptionHandler(RobotClientException.class)
    ProblemDetail handleRobotFailure(RobotClientException exception) {
        log.warn("Robot API call failed", exception);
        return problem(HttpStatus.BAD_GATEWAY, "Robot API call failed", exception.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
