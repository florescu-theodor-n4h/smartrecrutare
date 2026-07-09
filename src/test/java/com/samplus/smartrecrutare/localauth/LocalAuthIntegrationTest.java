package com.samplus.smartrecrutare.localauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samplus.smartrecrutare.employer.domain.EmployerStatus;
import com.samplus.smartrecrutare.employer.dto.EmployerCreateRequest;
import com.samplus.smartrecrutare.employer.dto.EmployerResponse;
import com.samplus.smartrecrutare.employer.service.EmployerService;
import com.samplus.smartrecrutare.job.dto.JobCreateRequest;
import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import com.samplus.smartrecrutare.localauth.dto.LocalLoginRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalRegistrationRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalUserCreateRequest;
import com.samplus.smartrecrutare.localauth.dto.ManagerEmployerAssignmentRequest;
import com.samplus.smartrecrutare.localauth.repository.LocalUserRepository;
import com.samplus.smartrecrutare.security.RolAplicatie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocalAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LocalUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployerService employerService;

    @Test
    void anonymousUserCanRegisterThenLoginWithUserRole() throws Exception {
        String username = unique("register");
        String password = "ParolaRegister!123";

        mockMvc.perform(post("/auth/local/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LocalRegistrationRequest(
                                username,
                                username + "@example.test",
                                password
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

        String token = login(username, password);

        mockMvc.perform(get("/auth/local/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(username));
    }

    @Test
    void localAdminCanLoginCreateUsersAndInspectCurrentIdentity() throws Exception {
        String parola = "ParolaLocala!123";
        LocalUser admin = seedUser("admin", parola, Set.of(RolAplicatie.ADMIN));
        String adminToken = login(admin.getUsername(), parola);

        LocalUserCreateRequest request = new LocalUserCreateRequest(
                unique("auditor"),
                unique("auditor") + "@example.test",
                "ParolaAuditor!123",
                Set.of(RolAplicatie.AUDITOR)
        );

        mockMvc.perform(post("/api/admin/local-users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(request.getUsername()));

        mockMvc.perform(get("/auth/local/me")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(admin.getUsername()));
    }

    @Test
    void localUserCanReadPublicJobsButCannotManageEmployers() throws Exception {
        String parola = "ParolaUser!123";
        LocalUser user = seedUser("user", parola, Set.of(RolAplicatie.USER));
        String token = login(user.getUsername(), parola);

        mockMvc.perform(get("/api/jobs/active")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEmployerRequest("LOCAL-USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void localManagerCanCreateJobsOnlyForAssignedEmployers() throws Exception {
        String parolaAdmin = "ParolaAdmin!123";
        String parolaManager = "ParolaManager!123";
        LocalUser admin = seedUser("admin-manager", parolaAdmin, Set.of(RolAplicatie.ADMIN));
        LocalUser manager = seedUser("manager", parolaManager, Set.of(RolAplicatie.MANAGER));
        String adminToken = login(admin.getUsername(), parolaAdmin);
        String managerToken = login(manager.getUsername(), parolaManager);

        EmployerResponse assignedEmployer = employerService.create(createEmployerRequest("ASSIGNED"));
        EmployerResponse otherEmployer = employerService.create(createEmployerRequest("OTHER"));

        mockMvc.perform(post("/api/admin/local-users/{id}/managed-employers", manager.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ManagerEmployerAssignmentRequest(assignedEmployer.getId()))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", bearer(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createJobRequest(assignedEmployer.getId(), "ASSIGNED"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", bearer(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createJobRequest(otherEmployer.getId(), "OTHER"))))
                .andExpect(status().isForbidden());
    }

    private LocalUser seedUser(String prefix, String password, Set<RolAplicatie> roles) {
        String username = unique(prefix);
        LocalUser user = LocalUser.creare(
                username,
                username + "@example.test",
                passwordEncoder.encode(password),
                roles
        );
        return userRepository.saveAndFlush(user);
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LocalLoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
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
                "Date de test LocalAuth",
                EmployerStatus.ACTIV
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

    private String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
