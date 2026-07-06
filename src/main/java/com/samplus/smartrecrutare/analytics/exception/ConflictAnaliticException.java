package com.samplus.smartrecrutare.analytics.exception;

/** Semnaleaza o modificare concurenta sau o regula de unicitate. */
public class ConflictAnaliticException extends RuntimeException {
    public ConflictAnaliticException(String message) {
        super(message);
    }
}
