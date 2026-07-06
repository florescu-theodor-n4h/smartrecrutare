package com.samplus.smartrecrutare.bot.service.impl;

import com.samplus.smartrecrutare.bot.client.RobotChatRequest;
import com.samplus.smartrecrutare.bot.client.RobotClient;
import com.samplus.smartrecrutare.bot.config.GptRobotProperties;
import com.samplus.smartrecrutare.bot.service.BotDecisionService;
import com.samplus.smartrecrutare.bot.service.ChatContextReader;
import com.samplus.smartrecrutare.bot.service.DecisionContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultBotDecisionService implements BotDecisionService {

    private final ChatContextReader contextReader;
    private final RobotClient robotClient;
    private final GptRobotProperties properties;

    public DefaultBotDecisionService(
            ChatContextReader contextReader,
            RobotClient robotClient,
            GptRobotProperties properties
    ) {
        this.contextReader = contextReader;
        this.robotClient = robotClient;
        this.properties = properties;
    }

    @Override
    public String createAssistantReply(UUID conversationId, UUID leafMessageId) {
        DecisionContext context = contextReader.loadBranch(
                conversationId,
                leafMessageId,
                properties.getHistoryLimit()
        );
        return robotClient.chat(new RobotChatRequest(
                context.getConversationId(),
                context.getCurrentPrompt(),
                context.getMessages()
        )).getContent();
    }
}
