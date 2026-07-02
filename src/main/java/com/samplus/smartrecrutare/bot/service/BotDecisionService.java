package com.samplus.smartrecrutare.bot.service;

import java.util.UUID;

/** Internal decision-making boundary; deliberately independent of HTTP. */
public interface BotDecisionService {
    String createAssistantReply(UUID conversationId, UUID leafMessageId);
}
