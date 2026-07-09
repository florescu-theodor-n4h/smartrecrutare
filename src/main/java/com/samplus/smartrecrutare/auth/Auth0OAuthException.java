package com.samplus.smartrecrutare.auth;

import org.springframework.http.HttpStatus;

public class Auth0OAuthException extends RuntimeException {
    private final HttpStatus responseStatus;
    private final Integer auth0Status;
    private final String auth0Body;

    private Auth0OAuthException(String message, HttpStatus responseStatus, Integer auth0Status, String auth0Body) {
        super(message);
        this.responseStatus = responseStatus;
        this.auth0Status = auth0Status;
        this.auth0Body = auth0Body;
    }

    public static Auth0OAuthException badRequest(String message) {
        return new Auth0OAuthException(message, HttpStatus.BAD_REQUEST, null, null);
    }

    public static Auth0OAuthException upstream(String operation, int auth0Status, String auth0Body) {
        String bodySummary = auth0Body == null || auth0Body.isBlank()
                ? "empty response body"
                : auth0Body;
        return new Auth0OAuthException(
                "Auth0 " + operation + " failed with status " + auth0Status + ": " + bodySummary,
                HttpStatus.BAD_GATEWAY,
                auth0Status,
                auth0Body
        );
    }

    public static Auth0OAuthException upstreamUnavailable(String operation, String message) {
        return new Auth0OAuthException(
                "Auth0 " + operation + " failed: " + message,
                HttpStatus.BAD_GATEWAY,
                null,
                null
        );
    }

    public HttpStatus getResponseStatus() {
        return responseStatus;
    }

    public Integer getAuth0Status() {
        return auth0Status;
    }

    public String getAuth0Body() {
        return auth0Body;
    }
}
