package com.samplus.smartrecrutare.bot.service;

import com.samplus.smartrecrutare.bot.dto.request.CreateConversationRequest;
import com.samplus.smartrecrutare.bot.dto.request.UpdateConversationRequest;
import com.samplus.smartrecrutare.bot.dto.request.UpdatePromptRequest;
import com.samplus.smartrecrutare.bot.dto.response.ConversationPageResponse;
import com.samplus.smartrecrutare.bot.dto.response.ConversationResponse;
import com.samplus.smartrecrutare.bot.dto.response.PromptResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ConversationService {
    ConversationResponse create(CreateConversationRequest request);

    ConversationPageResponse findAll(Pageable pageable);

    ConversationResponse findById(UUID conversationId);

    ConversationResponse update(UUID conversationId, UpdateConversationRequest request);

    void delete(UUID conversationId, Long version);

    PromptResponse getPrompt(UUID conversationId);

    PromptResponse updatePrompt(UUID conversationId, UpdatePromptRequest request);
}
