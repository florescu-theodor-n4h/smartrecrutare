package com.samplus.smartrecrutare.localauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalLoginRequest {
    @NotBlank
    @Size(max = 80)
    private String username;

    @NotBlank
    @Size(max = 200)
    private String password;
}
