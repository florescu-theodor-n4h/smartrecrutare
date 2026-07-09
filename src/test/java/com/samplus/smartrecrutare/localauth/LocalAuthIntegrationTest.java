package com.samplus.smartrecrutare.localauth;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jwt.SignedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samplus.smartrecrutare.employer.domain.EmployerStatus;
import com.samplus.smartrecrutare.employer.dto.EmployerCreateRequest;
import com.samplus.smartrecrutare.employer.dto.EmployerResponse;
import com.samplus.smartrecrutare.employer.service.EmployerService;
import com.samplus.smartrecrutare.localauth.config.LocalAuthProperties;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private LocalAuthProperties localAuthProperties;

    @Autowired
    @Qualifier("multiDecoder")
    private JwtDecoder multiDecoder;

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

    @Test
    void localLoginIssuesSymmetricJwtAcceptedByMultiDecoderAndResourceServer() throws Exception {
        String parola = "ParolaJwt!123";
        LocalUser admin = seedUser("jwt-admin", parola, Set.of(RolAplicatie.ADMIN));
        String token = login(admin.getUsername(), parola);

        SignedJWT signedJWT = SignedJWT.parse(token);
        Jwt decoded = multiDecoder.decode(token);

        assertThat(signedJWT.getHeader().getAlgorithm().getName()).isEqualTo("HS256");
        assertThat(signedJWT.getHeader().getKeyID()).isEqualTo(localAuthProperties.getKeyId());
        assertThat(decoded.getClaimAsString("iss")).isEqualTo(localAuthProperties.getIssuer());
        assertThat(decoded.getSubject()).isEqualTo(admin.getUsername());
        assertThat(decoded.getClaimAsStringList("roles")).containsExactly("ROLE_ADMIN");

        mockMvc.perform(get("/auth/local/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(admin.getUsername()));
    }

    @Test
    void tamperedLocalJwtIsRejectedByResourceServer() throws Exception {
        String parola = "ParolaTamper!123";
        LocalUser user = seedUser("tamper", parola, Set.of(RolAplicatie.USER));
        String token = tamper(login(user.getUsername(), parola));

        mockMvc.perform(get("/auth/local/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void localIssuerJwtSignedWithDifferentSymmetricKeyIsRejectedByResourceServer() throws Exception {
        String forgedToken = forgedLocalIssuerToken("forged-admin");

        mockMvc.perform(get("/auth/local/me")
                        .header("Authorization", bearer(forgedToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedBearerTokenIsRejectedByResourceServer() throws Exception {
        mockMvc.perform(get("/auth/local/me")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
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

    private String tamper(String token) {
        String[] segments = token.split("\\.", -1);
        char replacement = segments[2].charAt(0) == 'A' ? 'B' : 'A';
        segments[2] = replacement + segments[2].substring(1);
        return String.join(".", segments);
    }

    private String forgedLocalIssuerToken(String subject) {
        byte[] wrongKey = "wrong-local-auth-secret-with-more-than-32-bytes".getBytes(StandardCharsets.UTF_8);
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(wrongKey, "HmacSHA256")));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(localAuthProperties.getIssuer())
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(5)))
                .claim("auth_provider", "local")
                .claim("roles", Set.of("ROLE_ADMIN"))
                .claim("local_user_id", 999L)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
