package com.samplus.smartrecrutare.bot.service;

import com.samplus.smartrecrutare.bot.dto.request.CreateMessageRequest;
import com.samplus.smartrecrutare.bot.dto.request.UpdateMessageRequest;
import com.samplus.smartrecrutare.bot.dto.response.ChatHistoryResponse;
import com.samplus.smartrecrutare.bot.dto.response.ChatMessageResponse;
import com.samplus.smartrecrutare.bot.dto.response.MessageCountResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.Optional;

public interface MessageService {
    ChatMessageResponse create(UUID conversationId, CreateMessageRequest request);

    ChatHistoryResponse findHistory(UUID conversationId, Pageable pageable);

    ChatMessageResponse findById(UUID conversationId, UUID messageId);

    ChatMessageResponse update(UUID conversationId, UUID messageId, UpdateMessageRequest request);

    void delete(UUID conversationId, UUID messageId, Long version);

    MessageCountResponse count(UUID conversationId);

    Optional<UUID> findLatestMessageId(UUID conversationId);
}
