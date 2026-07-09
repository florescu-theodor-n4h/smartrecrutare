package com.samplus.smartrecrutare.bot.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

/**
 * External robot client settings.
 *
 * <p>Example {@code application.yml} configuration (documentation only):</p>
 * <pre>{@code
 * bot:
 *   gpt-robot:
 *     api:
 *       base-url: https://robot-api.internal.example
 *       chat-path: /v1/chat
 *       connect-timeout: 3s
 *       read-timeout: 30s
 *       history-limit: 50
 *       default-prompt: You are the recruitment assistant.
 * }</pre>
 *
 * <p>If {@code base-url} is absent, the application starts but calls to
 * {@code /bot/gpt-robot/user-chat} fail with HTTP 503. This lets CRUD/history
 * remain usable in environments that intentionally do not enable the robot.</p>
 */
@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "bot.gpt-robot.api")
public class GptRobotProperties {
    private URI baseUrl;

    @NotBlank
    private String chatPath = "/v1/chat";

    @NotNull
    private Duration connectTimeout = Duration.ofSeconds(3);

    @NotNull
    private Duration readTimeout = Duration.ofSeconds(30);

    @Min(1)
    @Max(200)
    private int historyLimit = 50;

    @NotBlank
    private String defaultPrompt = "You are a concise recruitment assistant.";

}
