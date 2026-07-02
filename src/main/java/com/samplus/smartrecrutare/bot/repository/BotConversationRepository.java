package com.samplus.smartrecrutare.bot.repository;

import com.samplus.smartrecrutare.bot.domain.BotConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface BotConversationRepository extends JpaRepository<BotConversation, UUID> {
    @Query(
            value = """
                    select conversation as conversation, count(message) as messageCount
                      from BotConversation conversation
                      left join ChatMessage message on message.conversation = conversation
                     group by conversation
                     order by conversation.updatedAt desc
                    """,
            countQuery = "select count(conversation) from BotConversation conversation"
    )
    Page<ConversationSummaryProjection> findSummaries(Pageable pageable);
}
