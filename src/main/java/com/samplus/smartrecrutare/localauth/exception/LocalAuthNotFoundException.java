package com.samplus.smartrecrutare.localauth.exception;

public class LocalAuthNotFoundException extends RuntimeException {
    public LocalAuthNotFoundException(Long id) {
        super("Utilizatorul local " + id + " nu exista");
    }
}
