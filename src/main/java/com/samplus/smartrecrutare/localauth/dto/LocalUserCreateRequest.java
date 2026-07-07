package com.samplus.smartrecrutare.localauth.dto;

import com.samplus.smartrecrutare.security.RolAplicatie;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalUserCreateRequest {
    @NotBlank
    @Size(max = 80)
    private String username;

    @Email
    @NotBlank
    @Size(max = 180)
    private String email;

    @NotBlank
    @Size(min = 12, max = 200)
    private String password;

    @NotEmpty
    private Set<RolAplicatie> roles;
}
