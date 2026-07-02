package com.samplus.smartrecrutare.bot.dto.response;

import java.util.List;

public record ConversationPageResponse(
        List<ConversationResponse> conversations,
        PageMetadataResponse page
) {
}
