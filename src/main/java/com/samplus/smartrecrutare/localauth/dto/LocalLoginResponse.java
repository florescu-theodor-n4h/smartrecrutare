package com.samplus.smartrecrutare.localauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalLoginResponse {
    private String tokenType;
    private String accessToken;
    private Instant expiresAt;
    private LocalUserResponse user;
}
