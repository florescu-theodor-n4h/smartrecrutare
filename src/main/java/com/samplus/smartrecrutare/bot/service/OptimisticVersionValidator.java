package com.samplus.smartrecrutare.bot.service;

import com.samplus.smartrecrutare.bot.exception.BotConflictException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OptimisticVersionValidator {
    public void verify(String resourceName, Object resourceId, Long actual, Long requested) {
        if (!Objects.equals(actual, requested)) {
            throw new BotConflictException(
                    resourceName + " " + resourceId + " has version " + actual
                            + ", but version " + requested + " was supplied"
            );
        }
    }
}
