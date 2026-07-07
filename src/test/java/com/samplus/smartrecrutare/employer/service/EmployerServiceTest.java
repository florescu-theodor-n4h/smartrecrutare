package com.samplus.smartrecrutare.employer.service;

import com.samplus.smartrecrutare.DepozitJoburi;
import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.employer.domain.EmployerStatus;
import com.samplus.smartrecrutare.employer.dto.EmployerCreateRequest;
import com.samplus.smartrecrutare.employer.dto.EmployerResponse;
import com.samplus.smartrecrutare.employer.dto.EmployerUpdateRequest;
import com.samplus.smartrecrutare.employer.exception.DuplicateFiscalCodeException;
import com.samplus.smartrecrutare.employer.exception.EmployerInUseException;
import com.samplus.smartrecrutare.employer.exception.EmployerNotFoundException;
import com.samplus.smartrecrutare.employer.mapper.EmployerMapper;
import com.samplus.smartrecrutare.employer.repository.EmployerRepository;
import com.samplus.smartrecrutare.localauth.service.LocalAuthorizationService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmployerServiceTest {

    private final EmployerRepository employerRepository = mock(EmployerRepository.class);
    private final DepozitJoburi depozitJoburi = mock(DepozitJoburi.class);
    private final LocalAuthorizationService localAuthorizationService = mock(LocalAuthorizationService.class);
    private final EmployerService service = new EmployerService(
            employerRepository,
            depozitJoburi,
            new EmployerMapper(),
            localAuthorizationService
    );

    @Test
    void createPersistsEmployerWhenFiscalCodeIsUnique() {
        EmployerCreateRequest request = createRequest();
        when(employerRepository.existsByCodFiscalIgnoreCase("RO12345678")).thenReturn(false);

        EmployerResponse response = service.create(request);

        assertThat(response.getNume()).isEqualTo("Samplus");
        assertThat(response.getStatus()).isEqualTo(EmployerStatus.ACTIV);
        verify(employerRepository).saveAndFlush(any(Employer.class));
        verify(localAuthorizationService).assignCreatedEmployerIfLocalManager(any(Employer.class));
    }

    @Test
    void createRejectsDuplicateFiscalCode() {
        when(employerRepository.existsByCodFiscalIgnoreCase("RO12345678")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest()))
                .isInstanceOf(DuplicateFiscalCodeException.class)
                .hasMessageContaining("RO12345678");
        verify(employerRepository, never()).saveAndFlush(any(Employer.class));
    }

    @Test
    void getByIdReturnsMappedEmployerOrFails() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(employer()));
        when(employerRepository.findById(404L)).thenReturn(Optional.empty());

        assertThat(service.getById(1L).getNume()).isEqualTo("Samplus");
        assertThatThrownBy(() -> service.getById(404L))
                .isInstanceOf(EmployerNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void updateRejectsFiscalCodeUsedByAnotherEmployer() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(employer()));
        when(employerRepository.existsByCodFiscalIgnoreCaseAndIdNot("RO99999999", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, updateRequest()))
                .isInstanceOf(DuplicateFiscalCodeException.class)
                .hasMessageContaining("RO99999999");
    }

    @Test
    void deleteRejectsEmployerWithJobs() {
        when(employerRepository.findById(1L)).thenReturn(Optional.of(employer()));
        when(depozitJoburi.existsByEmployerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(EmployerInUseException.class)
                .hasMessageContaining("joburi");
        verify(employerRepository, never()).delete(any(Employer.class));
    }

    private EmployerCreateRequest createRequest() {
        return new EmployerCreateRequest(
                "Samplus",
                "Samplus SRL",
                "RO12345678",
                "contact@samplus.ro",
                "0712345678",
                "https://samplus.ro",
                "Bucuresti",
                "Companie IT",
                EmployerStatus.ACTIV
        );
    }

    private EmployerUpdateRequest updateRequest() {
        return new EmployerUpdateRequest(
                "Samplus Nou",
                "Samplus Nou SRL",
                "RO99999999",
                "contact@samplus.ro",
                "0712345678",
                "https://samplus.ro",
                "Bucuresti",
                "Companie IT",
                EmployerStatus.IN_VERIFICARE
        );
    }

    private Employer employer() {
        return Employer.creare(
                "Samplus",
                "Samplus SRL",
                "RO12345678",
                "contact@samplus.ro",
                "0712345678",
                "https://samplus.ro",
                "Bucuresti",
                "Companie IT",
                EmployerStatus.ACTIV
        );
    }
}
