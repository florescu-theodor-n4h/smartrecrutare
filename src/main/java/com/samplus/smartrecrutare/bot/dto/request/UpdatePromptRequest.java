package com.samplus.smartrecrutare.bot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdatePromptRequest(
        @NotBlank @Size(max = 32_000) String currentPrompt,
        @NotNull @PositiveOrZero Long version
) {
}
