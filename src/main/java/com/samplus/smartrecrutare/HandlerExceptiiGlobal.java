package com.samplus.smartrecrutare;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class HandlerExceptiiGlobal {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(NoHandlerFoundException.class)
    public void handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("Unknown request: {} {}", request.getMethod(), request.getRequestURI());
    }
}