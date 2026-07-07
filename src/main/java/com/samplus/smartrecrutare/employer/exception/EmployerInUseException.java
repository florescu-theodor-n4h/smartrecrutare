package com.samplus.smartrecrutare.employer.exception;

public class EmployerInUseException extends RuntimeException {
    public EmployerInUseException(Long id) {
        super("Angajatorul " + id + " nu poate fi sters deoarece are joburi asociate");
    }
}
