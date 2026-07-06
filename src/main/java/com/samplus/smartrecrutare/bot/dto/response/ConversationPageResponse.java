package com.samplus.smartrecrutare.bot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationPageResponse {
    private List<ConversationResponse> conversations;
    private PageMetadataResponse page;
}
