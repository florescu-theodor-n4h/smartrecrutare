package com.samplus.smartrecrutare.bot.dto.response;

public record UserChatResponse(
        ConversationResponse conversation,
        ChatMessageResponse userMessage,
        ChatMessageResponse assistantMessage
) {
}
