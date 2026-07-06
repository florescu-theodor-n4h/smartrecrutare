package com.samplus.smartrecrutare.bot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {
    private UUID conversationId;
    private String currentPrompt;
    private List<ChatMessageResponse> entries;
    private long entryCount;
    private PageMetadataResponse page;
}
