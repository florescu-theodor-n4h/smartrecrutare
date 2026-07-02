package com.samplus.smartrecrutare.bot.dto.response;

import java.util.List;
import java.util.UUID;

public record ChatHistoryResponse(
        UUID conversationId,
        String currentPrompt,
        List<ChatMessageResponse> entries,
        long entryCount,
        PageMetadataResponse page
) {
}
