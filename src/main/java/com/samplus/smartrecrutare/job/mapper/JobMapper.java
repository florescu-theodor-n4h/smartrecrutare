package com.samplus.smartrecrutare.job.mapper;

import com.samplus.smartrecrutare.Job;
import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.employer.mapper.EmployerMapper;
import com.samplus.smartrecrutare.job.dto.JobCreateRequest;
import com.samplus.smartrecrutare.job.dto.JobPublishRequest;
import com.samplus.smartrecrutare.job.dto.JobResponse;
import com.samplus.smartrecrutare.job.dto.JobUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    private final EmployerMapper employerMapper;

    public JobMapper(EmployerMapper employerMapper) {
        this.employerMapper = employerMapper;
    }

    public Job toEntity(JobCreateRequest request, Employer employer) {
        return toEntity(
                request.getTitlu(),
                request.getDescriere(),
                request.getCompanie(),
                request.getLocatie(),
                request.getSalariu(),
                request.getTipContract(),
                request.getActiv(),
                employer
        );
    }

    public Job toEntity(JobPublishRequest request, Employer employer) {
        return toEntity(
                request.getTitlu(),
                request.getDescriere(),
                request.getCompanie(),
                request.getLocatie(),
                request.getSalariu(),
                request.getTipContract(),
                request.getActiv(),
                employer
        );
    }

    private Job toEntity(
            String titlu,
            String descriere,
            String companie,
            String locatie,
            String salariu,
            String tipContract,
            Boolean activ,
            Employer employer
    ) {
        Job job = new Job();
        job.setTitlu(titlu);
        job.setDescriere(descriere);
        job.setEmployer(employer);
        job.setCompanie(displayCompany(companie, employer));
        job.setLocatie(locatie);
        job.setSalariu(salariu);
        job.setTipContract(tipContract);
        job.setActiv(activ == null || activ);
        return job;
    }

    public void replace(Job job, JobUpdateRequest request, Employer employer) {
        job.setTitlu(request.getTitlu());
        job.setDescriere(request.getDescriere());
        job.setEmployer(employer);
        job.setCompanie(displayCompany(request.getCompanie(), employer));
        job.setLocatie(request.getLocatie());
        job.setSalariu(request.getSalariu());
        job.setTipContract(request.getTipContract());
        job.setActiv(Boolean.TRUE.equals(request.getActiv()));
    }

    public JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTitlu(),
                job.getDescriere(),
                job.getCompanie(),
                job.getLocatie(),
                job.getSalariu(),
                job.getTipContract(),
                job.isActiv(),
                employerMapper.toSummary(job.getEmployer()),
                job.getCreatedAt(),
                job.getCreatedBy(),
                job.getUpdatedAt(),
                job.getUpdatedBy(),
                job.getVersion()
        );
    }

    private String displayCompany(String companie, Employer employer) {
        if (companie != null && !companie.isBlank()) {
            return companie;
        }
        return employer.getNume();
    }
}
