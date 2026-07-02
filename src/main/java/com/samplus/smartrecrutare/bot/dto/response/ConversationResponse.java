package com.samplus.smartrecrutare.bot.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String title,
        String currentPrompt,
        long messageCount,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy,
        Long version
) {
}
