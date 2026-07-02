package com.samplus.smartrecrutare.bot.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PromptResponse(
        UUID conversationId,
        String currentPrompt,
        Instant updatedAt,
        String updatedBy,
        Long version
) {
}
