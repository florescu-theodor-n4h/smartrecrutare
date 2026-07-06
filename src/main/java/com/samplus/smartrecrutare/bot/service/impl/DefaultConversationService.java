package com.samplus.smartrecrutare.bot.service.impl;

import com.samplus.smartrecrutare.bot.domain.BotConversation;
import com.samplus.smartrecrutare.bot.dto.request.CreateConversationRequest;
import com.samplus.smartrecrutare.bot.dto.request.UpdateConversationRequest;
import com.samplus.smartrecrutare.bot.dto.request.UpdatePromptRequest;
import com.samplus.smartrecrutare.bot.dto.response.ConversationPageResponse;
import com.samplus.smartrecrutare.bot.dto.response.ConversationResponse;
import com.samplus.smartrecrutare.bot.dto.response.PromptResponse;
import com.samplus.smartrecrutare.bot.exception.BotResourceNotFoundException;
import com.samplus.smartrecrutare.bot.mapper.BotResponseMapper;
import com.samplus.smartrecrutare.bot.repository.BotConversationRepository;
import com.samplus.smartrecrutare.bot.repository.ChatMessageRepository;
import com.samplus.smartrecrutare.bot.service.ConversationService;
import com.samplus.smartrecrutare.bot.service.OptimisticVersionValidator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DefaultConversationService implements ConversationService {

    private final BotConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final BotResponseMapper mapper;
    private final OptimisticVersionValidator versionValidator;

    public DefaultConversationService(
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
    public ConversationResponse create(CreateConversationRequest request) {
        BotConversation conversation = BotConversation.create(
                request.getTitle().trim(),
                request.getCurrentPrompt().trim()
        );
        conversationRepository.saveAndFlush(conversation);
        return mapper.toConversation(conversation, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationPageResponse findAll(Pageable pageable) {
        var page = conversationRepository.findSummaries(pageable);
        var responses = page.getContent().stream()
                .map(summary -> mapper.toConversation(summary.getConversation(), summary.getMessageCount()))
                .collect(Collectors.toList());
        return new ConversationPageResponse(responses, mapper.toPageMetadata(page));
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse findById(UUID conversationId) {
        BotConversation conversation = requireConversation(conversationId);
        return mapper.toConversation(conversation, messageRepository.countByConversationId(conversationId));
    }

    @Override
    @Transactional
    public ConversationResponse update(UUID conversationId, UpdateConversationRequest request) {
        BotConversation conversation = requireConversation(conversationId);
        versionValidator.verify("Conversation", conversationId, conversation.getVersion(), request.getVersion());
        conversation.update(request.getTitle().trim(), request.getCurrentPrompt().trim());
        conversationRepository.flush();
        return mapper.toConversation(conversation, messageRepository.countByConversationId(conversationId));
    }

    @Override
    @Transactional
    public void delete(UUID conversationId, Long version) {
        BotConversation conversation = requireConversation(conversationId);
        versionValidator.verify("Conversation", conversationId, conversation.getVersion(), version);
        messageRepository.clearParents(conversationId);
        messageRepository.deleteAllForConversation(conversationId);
        conversationRepository.delete(conversation);
        conversationRepository.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public PromptResponse getPrompt(UUID conversationId) {
        return mapper.toPrompt(requireConversation(conversationId));
    }

    @Override
    @Transactional
    public PromptResponse updatePrompt(UUID conversationId, UpdatePromptRequest request) {
        BotConversation conversation = requireConversation(conversationId);
        versionValidator.verify("Conversation", conversationId, conversation.getVersion(), request.getVersion());
        conversation.changePrompt(request.getCurrentPrompt().trim());
        conversationRepository.flush();
        return mapper.toPrompt(conversation);
    }

    private BotConversation requireConversation(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BotResourceNotFoundException(
                        "Conversation " + conversationId + " was not found"
                ));
    }
}
