package com.samplus.smartrecrutare.localauth.dto;

import com.samplus.smartrecrutare.security.RolAplicatie;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalUserRolesRequest {
    @NotEmpty
    private Set<RolAplicatie> roles;
}
