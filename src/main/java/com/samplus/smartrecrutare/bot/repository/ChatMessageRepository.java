package com.samplus.smartrecrutare.bot.repository;

import com.samplus.smartrecrutare.bot.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @EntityGraph(attributePaths = {"conversation", "parent"})
    Optional<ChatMessage> findByIdAndConversationId(UUID id, UUID conversationId);

    @EntityGraph(attributePaths = {"conversation", "parent"})
    Page<ChatMessage> findByConversationIdOrderByCreatedAtAsc(UUID conversationId, Pageable pageable);

    @EntityGraph(attributePaths = {"conversation", "parent"})
    Optional<ChatMessage> findFirstByConversationIdOrderByCreatedAtDescIdDesc(UUID conversationId);

    @EntityGraph(attributePaths = {"conversation", "parent"})
    List<ChatMessage> findByParentId(UUID parentId);

    long countByConversationId(UUID conversationId);

    @Query("""
            select m.id as id,
                   parent.id as parentId,
                   m.role as role,
                   m.content as content
              from ChatMessage m
              left join m.parent parent
             where m.conversation.id = :conversationId
            """)
    List<MessageContextProjection> findContextRows(@Param("conversationId") UUID conversationId);

    @Modifying(flushAutomatically = true)
    @Query("""
            update ChatMessage message
               set message.parent = null
             where message.conversation.id = :conversationId
               and message.parent is not null
            """)
    int clearParents(@Param("conversationId") UUID conversationId);

    @Modifying(flushAutomatically = true)
    @Query("delete from ChatMessage message where message.conversation.id = :conversationId")
    int deleteAllForConversation(@Param("conversationId") UUID conversationId);
}
