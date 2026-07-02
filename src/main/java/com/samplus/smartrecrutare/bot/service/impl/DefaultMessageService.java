package com.samplus.smartrecrutare.bot.service.impl;

import com.samplus.smartrecrutare.bot.domain.BotConversation;
import com.samplus.smartrecrutare.bot.domain.ChatMessage;
import com.samplus.smartrecrutare.bot.dto.request.CreateMessageRequest;
import com.samplus.smartrecrutare.bot.dto.request.UpdateMessageRequest;
import com.samplus.smartrecrutare.bot.dto.response.ChatHistoryResponse;
import com.samplus.smartrecrutare.bot.dto.response.ChatMessageResponse;
import com.samplus.smartrecrutare.bot.dto.response.MessageCountResponse;
import com.samplus.smartrecrutare.bot.exception.BotConflictException;
import com.samplus.smartrecrutare.bot.exception.BotResourceNotFoundException;
import com.samplus.smartrecrutare.bot.mapper.BotResponseMapper;
import com.samplus.smartrecrutare.bot.repository.BotConversationRepository;
import com.samplus.smartrecrutare.bot.repository.ChatMessageRepository;
import com.samplus.smartrecrutare.bot.repository.MessageContextProjection;
import com.samplus.smartrecrutare.bot.service.MessageService;
import com.samplus.smartrecrutare.bot.service.OptimisticVersionValidator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class DefaultMessageService implements MessageService {

    private final BotConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final BotResponseMapper mapper;
    private final OptimisticVersionValidator versionValidator;

    public DefaultMessageService(
            BotConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            BotResponseMapper mapper,
            OptimisticVersionValidator versionValidator
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.mapper = mapper;
        this.versionValidator = versionValidator;
    }

    @Override
    @Transactional
    public ChatMessageResponse create(UUID conversationId, CreateMessageRequest request) {
        BotConversation conversation = requireConversation(conversationId);
        ChatMessage parent = requireParent(conversationId, request.parentMessageId());
        ChatMessage message = ChatMessage.create(
                conversation,
                parent,
                request.role(),
                request.content().trim()
        );
        messageRepository.saveAndFlush(message);
        return mapper.toMessage(message);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatHistoryResponse findHistory(UUID conversationId, Pageable pageable) {
        BotConversation conversation = requireConversation(conversationId);
        var page = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);
        var entries = page.getContent().stream().map(mapper::toMessage).toList();
        return new ChatHistoryResponse(
                conversationId,
                conversation.getCurrentPrompt(),
                entries,
                page.getTotalElements(),
                mapper.toPageMetadata(page)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ChatMessageResponse findById(UUID conversationId, UUID messageId) {
        requireConversation(conversationId);
        return mapper.toMessage(requireMessage(conversationId, messageId));
    }

    @Override
    @Transactional
    public ChatMessageResponse update(
            UUID conversationId,
            UUID messageId,
            UpdateMessageRequest request
    ) {
        ChatMessage message = requireMessage(conversationId, messageId);
        versionValidator.verify("Message", messageId, message.getVersion(), request.version());
        ChatMessage parent = requireParent(conversationId, request.parentMessageId());
        ensureAcyclic(conversationId, messageId, parent);
        message.update(parent, request.role(), request.content().trim());
        messageRepository.flush();
        return mapper.toMessage(message);
    }

    @Override
    @Transactional
    public void delete(UUID conversationId, UUID messageId, Long version) {
        ChatMessage message = requireMessage(conversationId, messageId);
        versionValidator.verify("Message", messageId, message.getVersion(), version);
        ChatMessage replacementParent = message.getParent() == null
                ? null
                : (ChatMessage) message.getParent();
        messageRepository.findByParentId(messageId)
                .forEach(child -> child.reparent(replacementParent));
        messageRepository.delete(message);
        messageRepository.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageCountResponse count(UUID conversationId) {
        requireConversation(conversationId);
        return new MessageCountResponse(
                conversationId,
                messageRepository.countByConversationId(conversationId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findLatestMessageId(UUID conversationId) {
        requireConversation(conversationId);
        return messageRepository.findFirstByConversationIdOrderByCreatedAtDescIdDesc(conversationId)
                .map(ChatMessage::getId);
    }

    private BotConversation requireConversation(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BotResourceNotFoundException(
                        "Conversation " + conversationId + " was not found"
                ));
    }

    private ChatMessage requireMessage(UUID conversationId, UUID messageId) {
        return messageRepository.findByIdAndConversationId(messageId, conversationId)
                .orElseThrow(() -> new BotResourceNotFoundException(
                        "Message " + messageId + " was not found in conversation " + conversationId
                ));
    }

    private ChatMessage requireParent(UUID conversationId, UUID parentMessageId) {
        return parentMessageId == null ? null : requireMessage(conversationId, parentMessageId);
    }

    private void ensureAcyclic(UUID conversationId, UUID messageId, ChatMessage newParent) {
        if (newParent == null) {
            return;
        }
        if (messageId.equals(newParent.getId())) {
            throw new BotConflictException("A message cannot be its own parent");
        }

        Map<UUID, UUID> parents = new HashMap<>();
        for (MessageContextProjection row : messageRepository.findContextRows(conversationId)) {
            parents.put(row.getId(), row.getParentId());
        }

        Set<UUID> visited = new HashSet<>();
        UUID cursor = newParent.getId();
        while (cursor != null && visited.add(cursor)) {
            if (messageId.equals(cursor)) {
                throw new BotConflictException("The requested parent would create a message cycle");
            }
            cursor = parents.get(cursor);
        }
        if (cursor != null) {
            throw new BotConflictException("The stored message tree already contains a cycle");
        }
    }
}
