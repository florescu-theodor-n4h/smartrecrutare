package com.samplus.smartrecrutare.analytics.exception;

/** Semnaleaza o regula de domeniu incalcata de cerere. */
public class CerereAnaliticaInvalidaException extends RuntimeException {
    public CerereAnaliticaInvalidaException(String message) {
        super(message);
    }
}
