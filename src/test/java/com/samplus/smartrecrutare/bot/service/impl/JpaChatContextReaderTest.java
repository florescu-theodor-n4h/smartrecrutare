package com.samplus.smartrecrutare.bot.service.impl;

import com.samplus.smartrecrutare.bot.domain.BotConversation;
import com.samplus.smartrecrutare.bot.domain.MessageRole;
import com.samplus.smartrecrutare.bot.repository.BotConversationRepository;
import com.samplus.smartrecrutare.bot.repository.ChatMessageRepository;
import com.samplus.smartrecrutare.bot.repository.MessageContextProjection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

        var context = reader.loadBranch(conversationId, leafId, 50);

        assertThat(context.currentPrompt()).isEqualTo("Recruitment prompt");
        assertThat(context.messages())
                .extracting(message -> message.content())
                .containsExactly("root", "answer", "follow-up");
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
