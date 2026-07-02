package com.samplus.smartrecrutare.bot.dto.response;

public record PageMetadataResponse(
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
