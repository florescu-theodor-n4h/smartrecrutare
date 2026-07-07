package com.samplus.smartrecrutare.localauth.dto;

import com.samplus.smartrecrutare.security.RolAplicatie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalUserResponse {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean locked;
    private Set<RolAplicatie> roles;
    private Set<Long> managedEmployerIds;
    private Instant lastLoginAt;
    private Instant creatLa;
    private String creatDe;
    private Instant modificatLa;
    private String modificatDe;
    private Long versiune;
}
