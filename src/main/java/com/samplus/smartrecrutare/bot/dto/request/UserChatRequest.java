package com.samplus.smartrecrutare.bot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Starts or continues a user branch. When {@code conversationId} is absent,
 * title and currentPrompt are optional and configured defaults are used.
 */
public record UserChatRequest(
        UUID conversationId,
        UUID parentMessageId,
        @Size(max = 200) String title,
        @Size(max = 32_000) String currentPrompt,
        @NotBlank @Size(max = 64_000) String message
) {
}
