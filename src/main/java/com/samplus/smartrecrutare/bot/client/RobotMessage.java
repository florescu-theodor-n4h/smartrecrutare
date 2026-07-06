package com.samplus.smartrecrutare.bot.client;

import com.samplus.smartrecrutare.bot.domain.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotMessage {
    private MessageRole role;
    private String content;
}
