package com.samplus.smartrecrutare.bot.domain;

import com.samplus.smartrecrutare.security.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

/** Root aggregate for a persisted robot conversation. */
@Entity
@Table(
        name = "bot_conversations",
        indexes = {
                @Index(name = "idx_bot_conversation_updated", columnList = "updated_at"),
                @Index(name = "idx_bot_conversation_created_by", columnList = "created_by")
        }
)
public class BotConversation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "current_prompt", nullable = false, columnDefinition = "TEXT")
    private String currentPrompt;

    protected BotConversation() {
        // Required by JPA.
    }

    private BotConversation(String title, String currentPrompt) {
        this.title = title;
        this.currentPrompt = currentPrompt;
    }

    public static BotConversation create(String title, String currentPrompt) {
        return new BotConversation(title, currentPrompt);
    }

    public void update(String title, String currentPrompt) {
        this.title = title;
        this.currentPrompt = currentPrompt;
    }

    public void changePrompt(String currentPrompt) {
        this.currentPrompt = currentPrompt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCurrentPrompt() {
        return currentPrompt;
    }
}
