package com.samplus.smartrecrutare.bot.domain;

import com.samplus.smartrecrutare.security.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.util.UUID;

/**
 * Polymorphic node in a conversation tree.
 *
 * <p>{@link InheritanceType#SINGLE_TABLE} keeps branch traversal and history
 * queries efficient while leaving a stable extension point for future entry
 * types such as tool calls. Audit and version columns are inherited from
 * {@link AuditableEntity}.</p>
 */
@Entity
@Table(
        name = "bot_conversation_entries",
        indexes = {
                @Index(name = "idx_bot_entry_conversation_created", columnList = "conversation_id,created_at"),
                @Index(name = "idx_bot_entry_parent", columnList = "parent_entry_id")
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "entry_type")
@BatchSize(size = 50)
public abstract class ConversationEntry extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_bot_entry_conversation")
    )
    private BotConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_entry_id",
            foreignKey = @ForeignKey(name = "fk_bot_entry_parent")
    )
    private ConversationEntry parent;

    protected ConversationEntry() {
        // Required by JPA.
    }

    protected ConversationEntry(BotConversation conversation, ConversationEntry parent) {
        this.conversation = conversation;
        this.parent = parent;
    }

    protected void changeParent(ConversationEntry parent) {
        this.parent = parent;
    }

    public UUID getId() {
        return id;
    }

    public BotConversation getConversation() {
        return conversation;
    }

    public ConversationEntry getParent() {
        return parent;
    }
}
