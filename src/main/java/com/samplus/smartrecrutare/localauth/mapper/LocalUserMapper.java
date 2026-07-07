package com.samplus.smartrecrutare.localauth.mapper;

import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import com.samplus.smartrecrutare.localauth.dto.LocalUserResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/** Converteste entitatea LocalUser in contractul public de raspuns. */
@Component
public class LocalUserMapper {
    public LocalUserResponse toResponse(LocalUser user) {
        return new LocalUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.isLocked(),
                user.getRoles(),
                user.getManagedEmployers().stream().map(Employer::getId).collect(Collectors.toSet()),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getCreatedBy(),
                user.getUpdatedAt(),
                user.getUpdatedBy(),
                user.getVersion()
        );
    }
}
