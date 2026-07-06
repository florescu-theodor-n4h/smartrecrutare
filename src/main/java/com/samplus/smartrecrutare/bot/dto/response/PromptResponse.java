package com.samplus.smartrecrutare.bot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptResponse {
    private UUID conversationId;
    private String currentPrompt;
    private Instant updatedAt;
    private String updatedBy;
    private Long version;
}
