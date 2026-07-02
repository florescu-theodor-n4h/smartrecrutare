package com.samplus.smartrecrutare.bot.client;

/** Port for the externally hosted robot API. */
public interface RobotClient {
    RobotChatResponse chat(RobotChatRequest request);
}
