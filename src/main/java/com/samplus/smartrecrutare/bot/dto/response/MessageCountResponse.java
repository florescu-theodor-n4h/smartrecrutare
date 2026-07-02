package com.samplus.smartrecrutare.bot.dto.response;

import java.util.UUID;

public record MessageCountResponse(UUID conversationId, long count) {
}
