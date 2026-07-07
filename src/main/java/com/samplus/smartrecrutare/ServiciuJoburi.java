package com.samplus.smartrecrutare;

import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.employer.exception.EmployerNotFoundException;
import com.samplus.smartrecrutare.employer.repository.EmployerRepository;
import com.samplus.smartrecrutare.job.dto.JobCreateRequest;
import com.samplus.smartrecrutare.job.dto.JobResponse;
import com.samplus.smartrecrutare.job.dto.JobUpdateRequest;
import com.samplus.smartrecrutare.job.mapper.JobMapper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServiciuJoburi {
    private static final Logger log = LoggerFactory.getLogger(ServiciuJoburi.class);

    private final DepozitJoburi depozitJoburi;
    private final EmployerRepository employerRepository;
    private final JobMapper jobMapper;

    public ServiciuJoburi(
            DepozitJoburi depozitJoburi,
            EmployerRepository employerRepository,
            JobMapper jobMapper
    ) {
        this.depozitJoburi = depozitJoburi;
        this.employerRepository = employerRepository;
        this.jobMapper = jobMapper;
    }

    @Transactional
    public Job creare(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job-ul nu poate fi null");
        }
        validareObligatorie(job);
        job.setId(null);
        if (job.getEmployer() != null && (job.getCompanie() == null || job.getCompanie().isBlank())) {
            job.setCompanie(job.getEmployer().getNume());
        }

        log.info("Creare job legacy: titlu='{}' companie='{}'", job.getTitlu(), job.getCompanie());
        return depozitJoburi.save(job);
    }

    @Transactional
    public JobResponse creareDinRequest(JobCreateRequest request) {
        Employer employer = findEmployer(request.getEmployerId());
        Job job = jobMapper.toEntity(request, employer);
        Job salvat = depozitJoburi.saveAndFlush(job);
        log.info("Job creat cu angajator: id={} employerId={}", salvat.getId(), employer.getId());
        return jobMapper.toResponse(salvat);
    }

    @Transactional
    public Job actualizare(Long id, Job dto) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("DTO-ul jobului nu poate fi null");
        }

        Job existent = gasesteEntitate(id);
        if (dto.getTitlu() != null) {
            existent.setTitlu(dto.getTitlu());
        }
        if (dto.getDescriere() != null) {
            existent.setDescriere(dto.getDescriere());
        }
        if (dto.getCompanie() != null) {
            existent.setCompanie(dto.getCompanie());
        }
        if (dto.getEmployer() != null) {
            existent.setEmployer(dto.getEmployer());
            if (dto.getCompanie() == null || dto.getCompanie().isBlank()) {
                existent.setCompanie(dto.getEmployer().getNume());
            }
        }
        if (dto.getLocatie() != null) {
            existent.setLocatie(dto.getLocatie());
        }
        if (dto.getSalariu() != null) {
            existent.setSalariu(dto.getSalariu());
        }
        if (dto.getTipContract() != null) {
            existent.setTipContract(dto.getTipContract());
        }
        existent.setActiv(dto.isActiv());

        return depozitJoburi.save(existent);
    }

    @Transactional
    public JobResponse inlocuire(Long id, JobUpdateRequest request) {
        Job existent = gasesteEntitate(id);
        Employer employer = findEmployer(request.getEmployerId());
        jobMapper.replace(existent, request, employer);
        depozitJoburi.flush();
        return jobMapper.toResponse(existent);
    }

    @Transactional
    public boolean stergere(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }
        if (!depozitJoburi.existsById(id)) {
            log.warn("Stergere ignorata - job inexistent: id={}", id);
            return false;
        }
        depozitJoburi.deleteById(id);
        log.info("Job sters: id={}", id);
        return true;
    }

    @Transactional(readOnly = true)
    public Collection<Job> getTateJoburile() {
        List<Job> joburi = depozitJoburi.findAll();
        log.info("GetToateJoburile count={}", joburi.size());
        return joburi;
    }

    @Transactional(readOnly = true)
    public Collection<JobResponse> getToateJoburileDto() {
        return depozitJoburi.findAll().stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Job> gasireById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }
        return depozitJoburi.findById(id);
    }

    @Transactional(readOnly = true)
    public JobResponse gasireDto(Long id) {
        return jobMapper.toResponse(gasesteEntitate(id));
    }

    @Transactional(readOnly = true)
    public List<Job> getJoburiActive() {
        List<Job> joburi = depozitJoburi.findByActivTrue();
        log.info("GetJoburiActive count={}", joburi.size());
        return joburi;
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getJoburiActiveDto() {
        return depozitJoburi.findByActivTrue().stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Job> cautareDupaTitlu(String fragment) {
        validareFragment(fragment);
        return depozitJoburi.findByTitluContainingIgnoreCase(fragment);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> cautareDupaTitluDto(String fragment) {
        validareFragment(fragment);
        return depozitJoburi.findByTitluContainingIgnoreCase(fragment).stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Job gasesteEntitate(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id-ul nu poate fi null");
        }
        return depozitJoburi.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job inexistent cu id=" + id));
    }

    private Employer findEmployer(Long employerId) {
        if (employerId == null) {
            throw new IllegalArgumentException("employerId nu poate fi null");
        }
        return employerRepository.findById(employerId)
                .orElseThrow(() -> new EmployerNotFoundException(employerId));
    }

    private void validareObligatorie(Job job) {
        if (job.getTitlu() == null || job.getTitlu().isBlank()) {
            throw new IllegalArgumentException("Titlul jobului este obligatoriu");
        }
        if (job.getCompanie() == null || job.getCompanie().isBlank()) {
            throw new IllegalArgumentException("Compania jobului este obligatorie");
        }
        if (job.getTipContract() == null || job.getTipContract().isBlank()) {
            throw new IllegalArgumentException("Tipul contractului este obligatoriu");
        }
    }

    private void validareFragment(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            throw new IllegalArgumentException("Fragmentul de cautare nu poate fi null/gol");
        }
    }
}
