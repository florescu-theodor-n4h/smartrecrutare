package com.samplus.smartrecrutare.bot.service.impl;

import com.samplus.smartrecrutare.bot.config.GptRobotProperties;
import com.samplus.smartrecrutare.bot.domain.MessageRole;
import com.samplus.smartrecrutare.bot.dto.request.CreateConversationRequest;
import com.samplus.smartrecrutare.bot.dto.request.CreateMessageRequest;
import com.samplus.smartrecrutare.bot.dto.request.UserChatRequest;
import com.samplus.smartrecrutare.bot.dto.response.ChatMessageResponse;
import com.samplus.smartrecrutare.bot.dto.response.ConversationResponse;
import com.samplus.smartrecrutare.bot.dto.response.UserChatResponse;
import com.samplus.smartrecrutare.bot.exception.BotValidationException;
import com.samplus.smartrecrutare.bot.service.BotDecisionService;
import com.samplus.smartrecrutare.bot.service.ConversationService;
import com.samplus.smartrecrutare.bot.service.MessageService;
import com.samplus.smartrecrutare.bot.service.UserChatService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Coordinates short database transactions around the external HTTP call.
 * The user message remains persisted if the robot is temporarily unavailable,
 * making a later retry observable and safe.
 */
@Service
public class DefaultUserChatService implements UserChatService {

    private static final int GENERATED_TITLE_LIMIT = 80;

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final BotDecisionService decisionService;
    private final GptRobotProperties properties;

    public DefaultUserChatService(
            ConversationService conversationService,
            MessageService messageService,
            BotDecisionService decisionService,
            GptRobotProperties properties
    ) {
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.decisionService = decisionService;
        this.properties = properties;
    }

    @Override
    public UserChatResponse chat(UserChatRequest request) {
        ConversationResponse conversation = resolveConversation(request);
        UUID parentMessageId = request.getParentMessageId();
        if (parentMessageId == null) {
            parentMessageId = messageService.findLatestMessageId(conversation.getId()).orElse(null);
        }

        ChatMessageResponse userMessage = messageService.create(
                conversation.getId(),
                new CreateMessageRequest(parentMessageId, MessageRole.USER, request.getMessage())
        );
        String assistantContent = decisionService.createAssistantReply(
                conversation.getId(),
                userMessage.getId()
        );
        ChatMessageResponse assistantMessage = messageService.create(
                conversation.getId(),
                new CreateMessageRequest(userMessage.getId(), MessageRole.ASSISTANT, assistantContent)
        );

        return new UserChatResponse(
                conversationService.findById(conversation.getId()),
                userMessage,
                assistantMessage
        );
    }

    private ConversationResponse resolveConversation(UserChatRequest request) {
        if (request.getConversationId() != null) {
            if (StringUtils.hasText(request.getTitle()) || StringUtils.hasText(request.getCurrentPrompt())) {
                throw new BotValidationException(
                        "title and currentPrompt may only be supplied when starting a conversation"
                );
            }
            return conversationService.findById(request.getConversationId());
        }

        String title = StringUtils.hasText(request.getTitle())
                ? request.getTitle().trim()
                : generatedTitle(request.getMessage());
        String prompt = StringUtils.hasText(request.getCurrentPrompt())
                ? request.getCurrentPrompt().trim()
                : properties.getDefaultPrompt();
        return conversationService.create(new CreateConversationRequest(title, prompt));
    }

    private String generatedTitle(String message) {
        String normalized = message.trim().replaceAll("\\s+", " ");
        return normalized.length() <= GENERATED_TITLE_LIMIT
                ? normalized
                : normalized.substring(0, GENERATED_TITLE_LIMIT);
    }
}
