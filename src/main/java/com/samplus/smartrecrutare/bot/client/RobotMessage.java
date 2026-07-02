package com.samplus.smartrecrutare.bot.client;

import com.samplus.smartrecrutare.bot.domain.MessageRole;

public record RobotMessage(MessageRole role, String content) {
}
