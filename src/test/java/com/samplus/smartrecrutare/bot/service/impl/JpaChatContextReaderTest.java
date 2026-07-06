package com.samplus.smartrecrutare.bot.service.impl;

import com.samplus.smartrecrutare.bot.domain.BotConversation;
import com.samplus.smartrecrutare.bot.domain.MessageRole;
import com.samplus.smartrecrutare.bot.exception.BotConflictException;
import com.samplus.smartrecrutare.bot.exception.BotResourceNotFoundException;
import com.samplus.smartrecrutare.bot.repository.BotConversationRepository;
import com.samplus.smartrecrutare.bot.repository.ChatMessageRepository;
import com.samplus.smartrecrutare.bot.repository.MessageContextProjection;
import com.samplus.smartrecrutare.bot.service.DecisionContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JpaChatContextReaderTest {

    private final BotConversationRepository conversationRepository = mock(BotConversationRepository.class);
    private final ChatMessageRepository messageRepository = mock(ChatMessageRepository.class);
    private final JpaChatContextReader reader = new JpaChatContextReader(
            conversationRepository,
            messageRepository
    );

    @Test
    void loadsOnlyTheSelectedBranchInChronologicalOrder() {
        UUID conversationId = UUID.randomUUID();
        UUID rootId = UUID.randomUUID();
        UUID assistantId = UUID.randomUUID();
        UUID leafId = UUID.randomUUID();
        UUID siblingId = UUID.randomUUID();
        BotConversation conversation = BotConversation.create("Hiring", "Recruitment prompt");

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageRepository.findContextRows(conversationId)).thenReturn(List.of(
                row(rootId, null, MessageRole.USER, "root"),
                row(assistantId, rootId, MessageRole.ASSISTANT, "answer"),
                row(leafId, assistantId, MessageRole.USER, "follow-up"),
                row(siblingId, rootId, MessageRole.USER, "other branch")
        ));

        DecisionContext context = reader.loadBranch(conversationId, leafId, 50);

        assertThat(context.getCurrentPrompt()).isEqualTo("Recruitment prompt");
        assertThat(context.getMessages())
                .extracting(message -> message.getContent())
                .containsExactly("root", "answer", "follow-up");
    }

    @Test
    void limitsHistoryToTheNewestMessagesOnTheBranch() {
        UUID conversationId = UUID.randomUUID();
        UUID rootId = UUID.randomUUID();
        UUID assistantId = UUID.randomUUID();
        UUID leafId = UUID.randomUUID();

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(BotConversation.create("Hiring", "Prompt")));
        when(messageRepository.findContextRows(conversationId)).thenReturn(List.of(
                row(rootId, null, MessageRole.USER, "root"),
                row(assistantId, rootId, MessageRole.ASSISTANT, "answer"),
                row(leafId, assistantId, MessageRole.USER, "follow-up")
        ));

        DecisionContext context = reader.loadBranch(conversationId, leafId, 2);

        assertThat(context.getMessages())
                .extracting(message -> message.getContent())
                .containsExactly("answer", "follow-up");
    }

    @Test
    void rejectsCyclesInStoredMessageTree() {
        UUID conversationId = UUID.randomUUID();
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(BotConversation.create("Hiring", "Prompt")));
        when(messageRepository.findContextRows(conversationId)).thenReturn(List.of(
                row(firstId, secondId, MessageRole.USER, "first"),
                row(secondId, firstId, MessageRole.ASSISTANT, "second")
        ));

        assertThatThrownBy(() -> reader.loadBranch(conversationId, secondId, 50))
                .isInstanceOf(BotConflictException.class)
                .hasMessageContaining("cycle");
    }

    @Test
    void rejectsMissingConversationBeforeLoadingMessages() {
        UUID conversationId = UUID.randomUUID();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reader.loadBranch(conversationId, UUID.randomUUID(), 50))
                .isInstanceOf(BotResourceNotFoundException.class)
                .hasMessageContaining(conversationId.toString());
    }

    private MessageContextProjection row(
            UUID id,
            UUID parentId,
            MessageRole role,
            String content
    ) {
        return new MessageContextProjection() {
            @Override
            public UUID getId() {
                return id;
            }

            @Override
            public UUID getParentId() {
                return parentId;
            }

            @Override
            public MessageRole getRole() {
                return role;
            }

            @Override
            public String getContent() {
                return content;
            }
        };
    }
}
