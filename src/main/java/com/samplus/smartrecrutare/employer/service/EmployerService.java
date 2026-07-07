package com.samplus.smartrecrutare.employer.service;

import com.samplus.smartrecrutare.DepozitJoburi;
import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.employer.dto.EmployerCreateRequest;
import com.samplus.smartrecrutare.employer.dto.EmployerResponse;
import com.samplus.smartrecrutare.employer.dto.EmployerUpdateRequest;
import com.samplus.smartrecrutare.employer.exception.DuplicateFiscalCodeException;
import com.samplus.smartrecrutare.employer.exception.EmployerInUseException;
import com.samplus.smartrecrutare.employer.exception.EmployerNotFoundException;
import com.samplus.smartrecrutare.employer.mapper.EmployerMapper;
import com.samplus.smartrecrutare.employer.repository.EmployerRepository;
import com.samplus.smartrecrutare.models.PaginaModel;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployerService {

    private final EmployerRepository employerRepository;
    private final DepozitJoburi depozitJoburi;
    private final EmployerMapper mapper;

    public EmployerService(
            EmployerRepository employerRepository,
            DepozitJoburi depozitJoburi,
            EmployerMapper mapper
    ) {
        this.employerRepository = employerRepository;
        this.depozitJoburi = depozitJoburi;
        this.mapper = mapper;
    }

    @Transactional
    public EmployerResponse create(EmployerCreateRequest request) {
        verificaCodFiscalUnic(request.getCodFiscal());
        Employer employer = Employer.creare(
                request.getNume(),
                request.getDenumireLegala(),
                request.getCodFiscal(),
                request.getEmailContact(),
                request.getTelefonContact(),
                request.getWebsite(),
                request.getLocatie(),
                request.getDescriere(),
                request.getStatus()
        );
        employerRepository.saveAndFlush(employer);
        return mapper.toResponse(employer);
    }

    @Transactional(readOnly = true)
    public EmployerResponse getById(Long id) {
        return mapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public PaginaModel<EmployerResponse> list(Pageable pageable) {
        var page = employerRepository.findAllByOrderByUpdatedAtDesc(pageable);
        List<EmployerResponse> content = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return new PaginaModel<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    public EmployerResponse update(Long id, EmployerUpdateRequest request) {
        Employer employer = findEntityById(id);
        if (employerRepository.existsByCodFiscalIgnoreCaseAndIdNot(request.getCodFiscal(), id)) {
            throw new DuplicateFiscalCodeException(request.getCodFiscal());
        }
        employer.inlocuire(
                request.getNume(),
                request.getDenumireLegala(),
                request.getCodFiscal(),
                request.getEmailContact(),
                request.getTelefonContact(),
                request.getWebsite(),
                request.getLocatie(),
                request.getDescriere(),
                request.getStatus()
        );
        employerRepository.flush();
        return mapper.toResponse(employer);
    }

    @Transactional
    public void delete(Long id) {
        Employer employer = findEntityById(id);
        if (depozitJoburi.existsByEmployerId(id)) {
            throw new EmployerInUseException(id);
        }
        employerRepository.delete(employer);
        employerRepository.flush();
    }

    @Transactional(readOnly = true)
    public Employer findEntityById(Long id) {
        return employerRepository.findById(id)
                .orElseThrow(() -> new EmployerNotFoundException(id));
    }

    private void verificaCodFiscalUnic(String codFiscal) {
        if (employerRepository.existsByCodFiscalIgnoreCase(codFiscal)) {
            throw new DuplicateFiscalCodeException(codFiscal);
        }
    }
}
