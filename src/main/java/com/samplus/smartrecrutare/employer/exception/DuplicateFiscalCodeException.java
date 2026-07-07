package com.samplus.smartrecrutare.employer.exception;

public class DuplicateFiscalCodeException extends RuntimeException {
    public DuplicateFiscalCodeException(String codFiscal) {
        super("Codul fiscal '" + codFiscal + "' este deja folosit");
    }
}
