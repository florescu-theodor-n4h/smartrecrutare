package com.samplus.smartrecrutare.bot.service;

import com.samplus.smartrecrutare.bot.client.RobotMessage;

import java.util.List;
import java.util.UUID;

public record DecisionContext(
        UUID conversationId,
        String currentPrompt,
        List<RobotMessage> messages
) {
}
