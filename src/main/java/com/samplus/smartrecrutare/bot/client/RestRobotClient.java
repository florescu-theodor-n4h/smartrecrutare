package com.samplus.smartrecrutare.bot.client;

import com.samplus.smartrecrutare.bot.exception.RobotClientException;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class RestRobotClient implements RobotClient {

    private final RestClient restClient;
    private final String chatPath;

    public RestRobotClient(RestClient restClient, String chatPath) {
        this.restClient = restClient;
        this.chatPath = chatPath;
    }

    @Override
    public RobotChatResponse chat(RobotChatRequest request) {
        try {
            RobotChatResponse response = restClient.post()
                    .uri(chatPath)
                    .body(request)
                    .retrieve()
                    .body(RobotChatResponse.class);

            if (response == null || !StringUtils.hasText(response.getContent())) {
                throw new RobotClientException("Robot API returned an empty response");
            }
            return response;
        } catch (RestClientException exception) {
            throw new RobotClientException("Robot API request failed", exception);
        }
    }
}
