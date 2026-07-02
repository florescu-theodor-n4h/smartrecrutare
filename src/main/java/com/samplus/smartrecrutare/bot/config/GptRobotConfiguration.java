package com.samplus.smartrecrutare.bot.config;

import com.samplus.smartrecrutare.bot.client.RestRobotClient;
import com.samplus.smartrecrutare.bot.client.RobotClient;
import com.samplus.smartrecrutare.bot.client.UnconfiguredRobotClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GptRobotProperties.class)
public class GptRobotConfiguration {

    @Bean
    RobotClient gptRobotClient(RestClient.Builder builder, GptRobotProperties properties) {
        URI baseUrl = properties.getBaseUrl();
        if (baseUrl == null) {
            return new UnconfiguredRobotClient();
        }
        validateBaseUrl(baseUrl);
        validateChatPath(properties.getChatPath());

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());

        RestClient restClient = builder
                .requestFactory(requestFactory)
                .baseUrl(baseUrl.toString())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return new RestRobotClient(restClient, properties.getChatPath());
    }

    private static void validateBaseUrl(URI baseUrl) {
        String scheme = baseUrl.getScheme();
        if (!baseUrl.isAbsolute()
                || baseUrl.getUserInfo() != null
                || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
            throw new IllegalStateException("bot.gpt-robot.api.base-url must be an absolute HTTP(S) URL");
        }
    }

    private static void validateChatPath(String chatPath) {
        URI path = URI.create(chatPath);
        if (path.isAbsolute() || !chatPath.startsWith("/") || chatPath.startsWith("//")) {
            throw new IllegalStateException("bot.gpt-robot.api.chat-path must be a server-relative path");
        }
    }
}
