package com.samplus.smartrecrutare.localauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cererea publica pentru inregistrarea unui utilizator local simplu.
 *
 * <p>Rolurile nu sunt acceptate de la client pe acest flux; serviciul de inregistrare aplica rolul
 * implicit sigur.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalRegistrationRequest {
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
}
