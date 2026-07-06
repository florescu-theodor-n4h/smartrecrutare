package com.samplus.smartrecrutare.bot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChatResponse {
    private ConversationResponse conversation;
    private ChatMessageResponse userMessage;
    private ChatMessageResponse assistantMessage;
}
