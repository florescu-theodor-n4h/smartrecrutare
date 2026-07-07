package com.samplus.smartrecrutare.localauth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerEmployerAssignmentRequest {
    @NotNull
    private Long employerId;
}
