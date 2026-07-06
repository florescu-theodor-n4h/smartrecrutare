package com.samplus.smartrecrutare.bot.service;

import com.samplus.smartrecrutare.bot.client.RobotMessage;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class DecisionContext {
    UUID conversationId;
    String currentPrompt;
    List<RobotMessage> messages;
}
