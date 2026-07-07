package com.samplus.smartrecrutare.localauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalUserUpdateRequest {
    @NotBlank
    @Size(max = 80)
    private String username;

    @Email
    @NotBlank
    @Size(max = 180)
    private String email;

    @NotNull
    private Boolean enabled;

    @NotNull
    private Boolean locked;
}
