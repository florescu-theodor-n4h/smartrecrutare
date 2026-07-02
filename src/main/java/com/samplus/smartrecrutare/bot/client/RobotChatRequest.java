package com.samplus.smartrecrutare.bot.client;

import java.util.List;
import java.util.UUID;

public record RobotChatRequest(
        UUID conversationReference,
        String prompt,
        List<RobotMessage> messages
) {
}
