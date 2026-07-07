package com.samplus.smartrecrutare.security;

import com.samplus.smartrecrutare.employer.domain.EmployerStatus;
import com.samplus.smartrecrutare.employer.dto.EmployerCreateRequest;
import com.samplus.smartrecrutare.employer.dto.EmployerResponse;
import com.samplus.smartrecrutare.employer.dto.EmployerUpdateRequest;
import com.samplus.smartrecrutare.employer.service.EmployerService;
import com.samplus.smartrecrutare.job.dto.JobCreateRequest;
import com.samplus.smartrecrutare.job.dto.JobResponse;
import com.samplus.smartrecrutare.job.dto.JobUpdateRequest;
import com.samplus.smartrecrutare.ServiciuJoburi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EmployerService employerService;

    @Autowired
    private ServiciuJoburi serviciuJoburi;

    @Test
    void adminCanManageEmployersAndJobs() throws Exception {
        EmployerResponse employer = createEmployer("ADMIN");
        JobResponse job = createJob(employer.getId(), "ADMIN");

        mockMvc.perform(put("/api/employers/{id}", employer.getId())
                        .with(role("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEmployer("ADMIN-UPD"))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/jobs/{id}", job.getId())
                        .with(role("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateJob(employer.getId(), "ADMIN-UPD"))))
                .andExpect(status().isOk());

        EmployerResponse deletable = createEmployer("ADMIN-DEL");
        mockMvc.perform(delete("/api/employers/{id}", deletable.getId()).with(role("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void managerCanCreateAndUpdateButCannotDeleteEmployerOrJob() throws Exception {
        mockMvc.perform(post("/api/employers")
                        .with(role("MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployerRequest("MANAGER"))))
                .andExpect(status().isCreated());

        EmployerResponse employer = createEmployer("MANAGER");
        JobResponse job = createJob(employer.getId(), "MANAGER");

        mockMvc.perform(put("/api/jobs/{id}", job.getId())
                        .with(role("MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateJob(employer.getId(), "MANAGER-UPD"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/employers/{id}", employer.getId()).with(role("MANAGER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/jobs/{id}", job.getId()).with(role("MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditorAndGovernmentalUserCanReadButCannotWrite() throws Exception {
        EmployerResponse employer = createEmployer("READ");

        mockMvc.perform(get("/api/employers").with(role("AUDITOR")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/employers/{id}", employer.getId()).with(role("GOVERNMENTAL_USER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/jobs").with(role("AUDITOR")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employers")
                        .with(role("AUDITOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployerRequest("AUDITOR"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/jobs")
                        .with(role("GOVERNMENTAL_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createJobRequest(employer.getId(), "GOV"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void normalUserAndGuestCanOnlyUsePublicActiveJobsEndpoint() throws Exception {
        mockMvc.perform(get("/api/jobs/active"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/jobs/active").with(role("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employers").with(role("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/jobs")
                        .with(role("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createJobRequest(1L, "USER"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employers"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/employers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployerRequest("GUEST"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void normalUserCannotAccessAdminAnalytics() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/dashboard").with(role("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditorAndGovernmentalUserCanReadSafeAdminAnalytics() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/dashboard").with(role("AUDITOR")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/analytics/dashboard").with(role("GOVERNMENTAL_USER")))
                .andExpect(status().isOk());
    }

    private EmployerResponse createEmployer(String marker) {
        return employerService.create(createEmployerRequest(marker));
    }

    private JobResponse createJob(Long employerId, String marker) {
        return serviciuJoburi.creareDinRequest(createJobRequest(employerId, marker));
    }

    private EmployerCreateRequest createEmployerRequest(String marker) {
        String suffix = marker + "-" + UUID.randomUUID();
        return new EmployerCreateRequest(
                "Companie " + suffix,
                "Companie " + suffix + " SRL",
                "RO" + Math.abs(suffix.hashCode()),
                "contact-" + Math.abs(suffix.hashCode()) + "@example.test",
                "0712345678",
                "https://example.test",
                "Bucuresti",
                "Date de test RBAC",
                EmployerStatus.ACTIV
        );
    }

    private EmployerUpdateRequest updateEmployer(String marker) {
        EmployerCreateRequest request = createEmployerRequest(marker);
        return new EmployerUpdateRequest(
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
    }

    private JobCreateRequest createJobRequest(Long employerId, String marker) {
        return new JobCreateRequest(
                "Java Developer " + marker,
                "Backend API",
                employerId,
                null,
                "Remote",
                "5000 EUR",
                "Full-time",
                true
        );
    }

    private JobUpdateRequest updateJob(Long employerId, String marker) {
        JobCreateRequest request = createJobRequest(employerId, marker);
        return new JobUpdateRequest(
                request.getTitlu(),
                request.getDescriere(),
                request.getEmployerId(),
                request.getCompanie(),
                request.getLocatie(),
                request.getSalariu(),
                request.getTipContract(),
                request.getActiv()
        );
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor role(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
