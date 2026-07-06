package com.samplus.smartrecrutare.bot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageCountResponse {
    private UUID conversationId;
    private long count;
}
