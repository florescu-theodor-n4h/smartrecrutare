package com.samplus.smartrecrutare.bot.service.impl;

import com.samplus.smartrecrutare.bot.client.RobotMessage;
import com.samplus.smartrecrutare.bot.domain.BotConversation;
import com.samplus.smartrecrutare.bot.exception.BotConflictException;
import com.samplus.smartrecrutare.bot.exception.BotResourceNotFoundException;
import com.samplus.smartrecrutare.bot.repository.BotConversationRepository;
import com.samplus.smartrecrutare.bot.repository.ChatMessageRepository;
import com.samplus.smartrecrutare.bot.repository.MessageContextProjection;
import com.samplus.smartrecrutare.bot.service.ChatContextReader;
import com.samplus.smartrecrutare.bot.service.DecisionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class JpaChatContextReader implements ChatContextReader {

    private final BotConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    public JpaChatContextReader(
            BotConversationRepository conversationRepository,
            ChatMessageRepository messageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DecisionContext loadBranch(UUID conversationId, UUID leafMessageId, int historyLimit) {
        BotConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BotResourceNotFoundException(
                        "Conversation " + conversationId + " was not found"
                ));

        Map<UUID, MessageContextProjection> rowsById = new HashMap<>();
        for (MessageContextProjection row : messageRepository.findContextRows(conversationId)) {
            rowsById.put(row.getId(), row);
        }
        if (!rowsById.containsKey(leafMessageId)) {
            throw new BotResourceNotFoundException(
                    "Message " + leafMessageId + " was not found in conversation " + conversationId
            );
        }

        var branchNewestFirst = new ArrayList<RobotMessage>();
        Set<UUID> visited = new HashSet<>();
        UUID cursor = leafMessageId;
        while (cursor != null && branchNewestFirst.size() < historyLimit) {
            if (!visited.add(cursor)) {
                throw new BotConflictException("The stored message tree contains a cycle");
            }
            MessageContextProjection row = rowsById.get(cursor);
            if (row == null) {
                throw new BotConflictException("The message tree references a missing parent " + cursor);
            }
            branchNewestFirst.add(new RobotMessage(row.getRole(), row.getContent()));
            cursor = row.getParentId();
        }
        Collections.reverse(branchNewestFirst);

        return new DecisionContext(
                conversationId,
                conversation.getCurrentPrompt(),
                java.util.List.copyOf(branchNewestFirst)
        );
    }
}
