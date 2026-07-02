package com.samplus.smartrecrutare.bot.exception;

public class RobotClientException extends RuntimeException {
    public RobotClientException(String message) {
        super(message);
    }

    public RobotClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
