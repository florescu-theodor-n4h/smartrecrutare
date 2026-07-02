package com.samplus.smartrecrutare.bot.client;

import com.samplus.smartrecrutare.bot.exception.RobotClientNotConfiguredException;

public class UnconfiguredRobotClient implements RobotClient {
    @Override
    public RobotChatResponse chat(RobotChatRequest request) {
        throw new RobotClientNotConfiguredException(
                "Robot API is disabled: configure bot.gpt-robot.api.base-url"
        );
    }
}
