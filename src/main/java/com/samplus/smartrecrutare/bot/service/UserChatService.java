package com.samplus.smartrecrutare.bot.service;

import com.samplus.smartrecrutare.bot.dto.request.UserChatRequest;
import com.samplus.smartrecrutare.bot.dto.response.UserChatResponse;

public interface UserChatService {
    UserChatResponse chat(UserChatRequest request);
}
