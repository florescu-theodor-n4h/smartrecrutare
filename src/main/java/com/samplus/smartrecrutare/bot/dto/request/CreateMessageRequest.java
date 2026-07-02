package com.samplus.smartrecrutare.bot.dto.request;

import com.samplus.smartrecrutare.bot.domain.MessageRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateMessageRequest(
        UUID parentMessageId,
        @NotNull MessageRole role,
        @NotBlank @Size(max = 64_000) String content
) {
}
