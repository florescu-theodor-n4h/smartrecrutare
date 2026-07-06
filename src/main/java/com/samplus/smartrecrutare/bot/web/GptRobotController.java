package com.samplus.smartrecrutare.bot.web;

import com.samplus.smartrecrutare.bot.dto.request.CreateConversationRequest;
import com.samplus.smartrecrutare.bot.dto.request.CreateMessageRequest;
import com.samplus.smartrecrutare.bot.dto.request.UpdateConversationRequest;
import com.samplus.smartrecrutare.bot.dto.request.UpdateMessageRequest;
import com.samplus.smartrecrutare.bot.dto.request.UpdatePromptRequest;
import com.samplus.smartrecrutare.bot.dto.request.UserChatRequest;
import com.samplus.smartrecrutare.bot.dto.response.ChatHistoryResponse;
import com.samplus.smartrecrutare.bot.dto.response.ChatMessageResponse;
import com.samplus.smartrecrutare.bot.dto.response.ConversationPageResponse;
import com.samplus.smartrecrutare.bot.dto.response.ConversationResponse;
import com.samplus.smartrecrutare.bot.dto.response.MessageCountResponse;
import com.samplus.smartrecrutare.bot.dto.response.PromptResponse;
import com.samplus.smartrecrutare.bot.dto.response.UserChatResponse;
import com.samplus.smartrecrutare.bot.service.ConversationService;
import com.samplus.smartrecrutare.bot.service.MessageService;
import com.samplus.smartrecrutare.bot.service.UserChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/bot/gpt-robot")
@Tag(name = "GPT Robot", description = "Persistent, versioned conversation trees and robot chat")
public class GptRobotController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserChatService userChatService;

    public GptRobotController(
            ConversationService conversationService,
            MessageService messageService,
            UserChatService userChatService
    ) {
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userChatService = userChatService;
    }

    @Operation(summary = "Start or continue a conversation and call the configured robot")
    @PostMapping("/user-chat")
    public ResponseEntity<UserChatResponse> userChat(@Valid @RequestBody UserChatRequest request) {
        return ResponseEntity.ok(userChatService.chat(request));
    }

    @Operation(summary = "Create a persisted conversation")
    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody CreateConversationRequest request
    ) {
        ConversationResponse response = conversationService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{conversationId}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List conversations with aggregate message counts")
    @GetMapping("/conversations")
    public ResponseEntity<ConversationPageResponse> findConversations(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(conversationService.findAll(PageRequest.of(page, size)));
    }

    @Operation(summary = "Get one conversation")
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponse> findConversation(
            @PathVariable UUID conversationId
    ) {
        return ResponseEntity.ok(conversationService.findById(conversationId));
    }

    @Operation(summary = "Replace a conversation using optimistic versioning")
    @PutMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponse> updateConversation(
            @PathVariable UUID conversationId,
            @Valid @RequestBody UpdateConversationRequest request
    ) {
        return ResponseEntity.ok(conversationService.update(conversationId, request));
    }

    @Operation(summary = "Delete a conversation and its persisted history")
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable UUID conversationId,
            @RequestParam @NotNull @PositiveOrZero Long version
    ) {
        conversationService.delete(conversationId, version);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get the current saved prompt")
    @GetMapping("/conversations/{conversationId}/prompt")
    public ResponseEntity<PromptResponse> getPrompt(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(conversationService.getPrompt(conversationId));
    }

    @Operation(summary = "Replace the current saved prompt")
    @PutMapping("/conversations/{conversationId}/prompt")
    public ResponseEntity<PromptResponse> updatePrompt(
            @PathVariable UUID conversationId,
            @Valid @RequestBody UpdatePromptRequest request
    ) {
        return ResponseEntity.ok(conversationService.updatePrompt(conversationId, request));
    }

    @Operation(summary = "Create a history entry")
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ChatMessageResponse> createMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody CreateMessageRequest request
    ) {
        ChatMessageResponse response = messageService.create(conversationId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{messageId}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get paged history entries and the total count")
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ChatHistoryResponse> findHistory(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(messageService.findHistory(
                conversationId,
                PageRequest.of(page, size)
        ));
    }

    @Operation(summary = "Get a history entry")
    @GetMapping("/conversations/{conversationId}/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> findMessage(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId
    ) {
        return ResponseEntity.ok(messageService.findById(conversationId, messageId));
    }

    @Operation(summary = "Replace a history entry using optimistic versioning")
    @PutMapping("/conversations/{conversationId}/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> updateMessage(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            @Valid @RequestBody UpdateMessageRequest request
    ) {
        return ResponseEntity.ok(messageService.update(conversationId, messageId, request));
    }

    @Operation(summary = "Delete a history entry and reparent its direct children")
    @DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            @RequestParam @NotNull @PositiveOrZero Long version
    ) {
        messageService.delete(conversationId, messageId, version);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Count persisted history entries")
    @GetMapping("/conversations/{conversationId}/messages/count")
    public ResponseEntity<MessageCountResponse> countMessages(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(messageService.count(conversationId));
    }

    @Operation(summary = "Discover supported HTTP methods")
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> options() {
        return ResponseEntity.noContent()
                .allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS)
                .build();
    }
}
