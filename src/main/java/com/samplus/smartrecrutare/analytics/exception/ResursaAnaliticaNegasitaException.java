package com.samplus.smartrecrutare.analytics.exception;

/** Semnaleaza absenta unei resurse din modulul de analitice. */
public class ResursaAnaliticaNegasitaException extends RuntimeException {
    public ResursaAnaliticaNegasitaException(String message) {
        super(message);
    }
}
