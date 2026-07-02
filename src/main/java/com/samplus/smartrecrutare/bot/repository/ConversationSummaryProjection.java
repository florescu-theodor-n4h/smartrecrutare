package com.samplus.smartrecrutare.bot.repository;

import com.samplus.smartrecrutare.bot.domain.BotConversation;

public interface ConversationSummaryProjection {
    BotConversation getConversation();

    long getMessageCount();
}
