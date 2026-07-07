package com.samplus.smartrecrutare.employer.exception;

public class EmployerNotFoundException extends RuntimeException {
    public EmployerNotFoundException(Long id) {
        super("Angajatorul " + id + " nu exista");
    }
}
