package com.samplus.smartrecrutare.localauth.exception;

public class LocalAuthBadCredentialsException extends RuntimeException {
    public LocalAuthBadCredentialsException() {
        super("Credentiale locale invalide");
    }
}
