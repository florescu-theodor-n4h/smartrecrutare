package com.samplus.smartrecrutare.bot.service.impl;

import com.samplus.smartrecrutare.bot.client.RobotChatRequest;
import com.samplus.smartrecrutare.bot.client.RobotChatResponse;
import com.samplus.smartrecrutare.bot.client.RobotClient;
import com.samplus.smartrecrutare.bot.client.RobotMessage;
import com.samplus.smartrecrutare.bot.config.GptRobotProperties;
import com.samplus.smartrecrutare.bot.domain.MessageRole;
import com.samplus.smartrecrutare.bot.service.ChatContextReader;
import com.samplus.smartrecrutare.bot.service.DecisionContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultBotDecisionServiceTest {

    @Test
    void forwardsSelectedBranchAndConfiguredHistoryLimitToRobot() {
        ChatContextReader contextReader = mock(ChatContextReader.class);
        RobotClient robotClient = mock(RobotClient.class);
        GptRobotProperties properties = new GptRobotProperties();
        properties.setHistoryLimit(12);
        DefaultBotDecisionService service = new DefaultBotDecisionService(
                contextReader,
                robotClient,
                properties
        );
        UUID conversationId = UUID.randomUUID();
        UUID leafId = UUID.randomUUID();
        List<RobotMessage> messages = List.of(
                new RobotMessage(MessageRole.USER, "Which candidates match?")
        );
        DecisionContext context = new DecisionContext(conversationId, "Recruitment prompt", messages);
        RobotChatRequest expectedRequest = new RobotChatRequest(
                conversationId,
                "Recruitment prompt",
                messages
        );
        when(contextReader.loadBranch(conversationId, leafId, 12)).thenReturn(context);
        when(robotClient.chat(expectedRequest)).thenReturn(new RobotChatResponse("Three candidates match."));

        String reply = service.createAssistantReply(conversationId, leafId);

        assertThat(reply).isEqualTo("Three candidates match.");
        verify(contextReader).loadBranch(conversationId, leafId, 12);
        verify(robotClient).chat(expectedRequest);
    }
}
