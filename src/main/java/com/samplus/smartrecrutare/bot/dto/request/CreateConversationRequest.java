package com.samplus.smartrecrutare.bot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateConversationRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 32_000) String currentPrompt
) {
}
