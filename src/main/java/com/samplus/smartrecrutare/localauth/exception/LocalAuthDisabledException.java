package com.samplus.smartrecrutare.localauth.exception;

public class LocalAuthDisabledException extends RuntimeException {
    public LocalAuthDisabledException() {
        super("LocalAuth nu este activat sau nu are secret JWT configurat");
    }
}
