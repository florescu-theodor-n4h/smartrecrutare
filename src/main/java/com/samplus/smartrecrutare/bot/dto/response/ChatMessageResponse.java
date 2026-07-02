package com.samplus.smartrecrutare.bot.dto.response;

import com.samplus.smartrecrutare.bot.domain.MessageRole;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        UUID conversationId,
        UUID parentMessageId,
        MessageRole role,
        String content,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy,
        Long version
) {
}
