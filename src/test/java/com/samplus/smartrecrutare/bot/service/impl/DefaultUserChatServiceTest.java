package com.samplus.smartrecrutare.bot.service.impl;

import com.samplus.smartrecrutare.bot.config.GptRobotProperties;
import com.samplus.smartrecrutare.bot.domain.MessageRole;
import com.samplus.smartrecrutare.bot.dto.request.CreateMessageRequest;
import com.samplus.smartrecrutare.bot.dto.request.UserChatRequest;
import com.samplus.smartrecrutare.bot.dto.response.ChatMessageResponse;
import com.samplus.smartrecrutare.bot.dto.response.ConversationResponse;
import com.samplus.smartrecrutare.bot.dto.response.UserChatResponse;
import com.samplus.smartrecrutare.bot.exception.BotValidationException;
import com.samplus.smartrecrutare.bot.service.BotDecisionService;
import com.samplus.smartrecrutare.bot.service.ConversationService;
import com.samplus.smartrecrutare.bot.service.MessageService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultUserChatServiceTest {

    private final ConversationService conversationService = mock(ConversationService.class);
    private final MessageService messageService = mock(MessageService.class);
    private final BotDecisionService decisionService = mock(BotDecisionService.class);
    private final DefaultUserChatService service = new DefaultUserChatService(
            conversationService,
            messageService,
            decisionService,
            new GptRobotProperties()
    );

    @Test
    void persistsUserThenAssistantAroundTheRobotCall() {
        UUID conversationId = UUID.randomUUID();
        UUID previousId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assistantId = UUID.randomUUID();
        ConversationResponse before = conversation(conversationId, 1);
        ConversationResponse after = conversation(conversationId, 3);
        ChatMessageResponse user = message(userId, conversationId, previousId, MessageRole.USER, "Hello");
        ChatMessageResponse assistant = message(
                assistantId,
                conversationId,
                userId,
                MessageRole.ASSISTANT,
                "Hi"
        );

        when(conversationService.findById(conversationId)).thenReturn(before, after);
        when(messageService.findLatestMessageId(conversationId)).thenReturn(Optional.of(previousId));
        when(messageService.create(
                conversationId,
                new CreateMessageRequest(previousId, MessageRole.USER, "Hello")
        )).thenReturn(user);
        when(decisionService.createAssistantReply(conversationId, userId)).thenReturn("Hi");
        when(messageService.create(
                conversationId,
                new CreateMessageRequest(userId, MessageRole.ASSISTANT, "Hi")
        )).thenReturn(assistant);

        UserChatResponse response = service.chat(new UserChatRequest(
                conversationId,
                null,
                null,
                null,
                "Hello"
        ));

        assertThat(response.getConversation().getMessageCount()).isEqualTo(3);
        assertThat(response.getUserMessage()).isEqualTo(user);
        assertThat(response.getAssistantMessage()).isEqualTo(assistant);
        var order = inOrder(messageService, decisionService);
        order.verify(messageService).create(
                conversationId,
                new CreateMessageRequest(previousId, MessageRole.USER, "Hello")
        );
        order.verify(decisionService).createAssistantReply(conversationId, userId);
        order.verify(messageService).create(
                conversationId,
                new CreateMessageRequest(userId, MessageRole.ASSISTANT, "Hi")
        );
        verify(messageService).findLatestMessageId(conversationId);
    }

    @Test
    void usesExplicitParentWithoutLoadingTheLatestMessage() {
        UUID conversationId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ConversationResponse conversation = conversation(conversationId, 2);
        ChatMessageResponse user = message(userId, conversationId, parentId, MessageRole.USER, "Question");
        ChatMessageResponse assistant = message(
                UUID.randomUUID(), conversationId, userId, MessageRole.ASSISTANT, "Answer"
        );

        when(conversationService.findById(conversationId)).thenReturn(conversation);
        when(messageService.create(
                conversationId,
                new CreateMessageRequest(parentId, MessageRole.USER, "Question")
        )).thenReturn(user);
        when(decisionService.createAssistantReply(conversationId, userId)).thenReturn("Answer");
        when(messageService.create(
                conversationId,
                new CreateMessageRequest(userId, MessageRole.ASSISTANT, "Answer")
        )).thenReturn(assistant);

        service.chat(new UserChatRequest(conversationId, parentId, null, null, "Question"));

        verify(messageService, never()).findLatestMessageId(conversationId);
    }

    @Test
    void rejectsConversationMetadataWhenContinuingAnExistingConversation() {
        UUID conversationId = UUID.randomUUID();

        assertThatThrownBy(() -> service.chat(new UserChatRequest(
                conversationId,
                null,
                "Changed title",
                null,
                "Hello"
        )))
                .isInstanceOf(BotValidationException.class)
                .hasMessageContaining("only be supplied when starting");

        verifyNoInteractions(conversationService, messageService, decisionService);
    }

    @Test
    void doesNotPersistAssistantMessageWhenRobotCallFails() {
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ConversationResponse conversation = conversation(conversationId, 0);
        ChatMessageResponse user = message(userId, conversationId, null, MessageRole.USER, "Hello");

        when(conversationService.findById(conversationId)).thenReturn(conversation);
        when(messageService.findLatestMessageId(conversationId)).thenReturn(Optional.empty());
        when(messageService.create(
                conversationId,
                new CreateMessageRequest(null, MessageRole.USER, "Hello")
        )).thenReturn(user);
        when(decisionService.createAssistantReply(conversationId, userId))
                .thenThrow(new IllegalStateException("robot unavailable"));

        assertThatThrownBy(() -> service.chat(new UserChatRequest(
                conversationId, null, null, null, "Hello"
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("robot unavailable");

        verify(messageService, times(1)).create(any(UUID.class), any(CreateMessageRequest.class));
    }

    private ConversationResponse conversation(UUID id, long count) {
        return new ConversationResponse(
                id,
                "Chat",
                "Prompt",
                count,
                Instant.EPOCH,
                "test",
                Instant.EPOCH,
                "test",
                0L
        );
    }

    private ChatMessageResponse message(
            UUID id,
            UUID conversationId,
            UUID parentId,
            MessageRole role,
            String content
    ) {
        return new ChatMessageResponse(
                id,
                conversationId,
                parentId,
                role,
                content,
                Instant.EPOCH,
                "test",
                Instant.EPOCH,
                "test",
                0L
        );
    }
}
