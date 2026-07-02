package com.samplus.smartrecrutare.bot.mapper;

import com.samplus.smartrecrutare.bot.domain.BotConversation;
import com.samplus.smartrecrutare.bot.domain.ChatMessage;
import com.samplus.smartrecrutare.bot.dto.response.ChatMessageResponse;
import com.samplus.smartrecrutare.bot.dto.response.ConversationResponse;
import com.samplus.smartrecrutare.bot.dto.response.PageMetadataResponse;
import com.samplus.smartrecrutare.bot.dto.response.PromptResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BotResponseMapper {

    public ConversationResponse toConversation(BotConversation conversation, long messageCount) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCurrentPrompt(),
                messageCount,
                conversation.getCreatedAt(),
                conversation.getCreatedBy(),
                conversation.getUpdatedAt(),
                conversation.getUpdatedBy(),
                conversation.getVersion()
        );
    }

    public PromptResponse toPrompt(BotConversation conversation) {
        return new PromptResponse(
                conversation.getId(),
                conversation.getCurrentPrompt(),
                conversation.getUpdatedAt(),
                conversation.getUpdatedBy(),
                conversation.getVersion()
        );
    }

    public ChatMessageResponse toMessage(ChatMessage message) {
        UUID parentId = message.getParent() == null ? null : message.getParent().getId();
        return new ChatMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                parentId,
                message.getRole(),
                message.getContent(),
                message.getCreatedAt(),
                message.getCreatedBy(),
                message.getUpdatedAt(),
                message.getUpdatedBy(),
                message.getVersion()
        );
    }

    public PageMetadataResponse toPageMetadata(Page<?> page) {
        return new PageMetadataResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
