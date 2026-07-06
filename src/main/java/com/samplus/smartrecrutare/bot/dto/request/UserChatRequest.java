package com.samplus.smartrecrutare.bot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Starts or continues a user branch. When {@code conversationId} is absent,
 * title and currentPrompt are optional and configured defaults are used.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChatRequest {
    private UUID conversationId;
    private UUID parentMessageId;

    @Size(max = 200)
    private String title;

    @Size(max = 32_000)
    private String currentPrompt;

    @NotBlank
    @Size(max = 64_000)
    private String message;
}
