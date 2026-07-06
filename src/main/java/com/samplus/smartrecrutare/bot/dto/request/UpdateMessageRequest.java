package com.samplus.smartrecrutare.bot.dto.request;

import com.samplus.smartrecrutare.bot.domain.MessageRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMessageRequest {
    private UUID parentMessageId;

    @NotNull
    private MessageRole role;

    @NotBlank
    @Size(max = 64_000)
    private String content;

    @NotNull
    @PositiveOrZero
    private Long version;
}
