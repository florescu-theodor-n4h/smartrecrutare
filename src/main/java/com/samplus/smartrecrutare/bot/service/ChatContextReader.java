package com.samplus.smartrecrutare.bot.service;

import java.util.UUID;

/** Loads immutable robot context inside a short read-only transaction. */
public interface ChatContextReader {
    DecisionContext loadBranch(UUID conversationId, UUID leafMessageId, int historyLimit);
}
