package com.samplus.smartrecrutare.bot.repository;

import com.samplus.smartrecrutare.bot.domain.MessageRole;

import java.util.UUID;

/** Closed projection used to rebuild one conversation branch in a single query. */
public interface MessageContextProjection {
    UUID getId();

    UUID getParentId();

    MessageRole getRole();

    String getContent();
}
