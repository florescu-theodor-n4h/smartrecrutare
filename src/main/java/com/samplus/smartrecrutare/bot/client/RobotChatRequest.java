package com.samplus.smartrecrutare.bot.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotChatRequest {
    private UUID conversationReference;
    private String prompt;
    private List<RobotMessage> messages;
}
