package com.samplus.smartrecrutare.employer.mapper;

import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.employer.dto.EmployerResponse;
import com.samplus.smartrecrutare.employer.dto.EmployerSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class EmployerMapper {

    public EmployerResponse toResponse(Employer employer) {
        return new EmployerResponse(
                employer.getId(),
                employer.getNume(),
                employer.getDenumireLegala(),
                employer.getCodFiscal(),
                employer.getEmailContact(),
                employer.getTelefonContact(),
                employer.getWebsite(),
                employer.getLocatie(),
                employer.getDescriere(),
                employer.getStatus(),
                employer.getCreatedAt(),
                employer.getCreatedBy(),
                employer.getUpdatedAt(),
                employer.getUpdatedBy(),
                employer.getVersion()
        );
    }

    public EmployerSummaryResponse toSummary(Employer employer) {
        if (employer == null) {
            return null;
        }
        return new EmployerSummaryResponse(
                employer.getId(),
                employer.getNume(),
                employer.getDenumireLegala()
        );
    }
}
