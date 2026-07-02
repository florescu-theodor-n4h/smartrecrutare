package com.samplus.smartrecrutare.bot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** A persisted message node in a conversation tree. */
@Entity
@DiscriminatorValue("MESSAGE")
public class ChatMessage extends ConversationEntry {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    protected ChatMessage() {
        // Required by JPA.
    }

    private ChatMessage(
            BotConversation conversation,
            ChatMessage parent,
            MessageRole role,
            String content
    ) {
        super(conversation, parent);
        this.role = role;
        this.content = content;
    }

    public static ChatMessage create(
            BotConversation conversation,
            ChatMessage parent,
            MessageRole role,
            String content
    ) {
        return new ChatMessage(conversation, parent, role, content);
    }

    public void update(ChatMessage parent, MessageRole role, String content) {
        changeParent(parent);
        this.role = role;
        this.content = content;
    }

    public void reparent(ChatMessage parent) {
        changeParent(parent);
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
