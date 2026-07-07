package com.samplus.smartrecrutare.localauth.service;

import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.employer.service.EmployerService;
import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import com.samplus.smartrecrutare.localauth.dto.LocalUserCreateRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalUserPasswordRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalUserResponse;
import com.samplus.smartrecrutare.localauth.dto.LocalUserRolesRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalUserUpdateRequest;
import com.samplus.smartrecrutare.localauth.dto.ManagerEmployerAssignmentRequest;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthConflictException;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthNotFoundException;
import com.samplus.smartrecrutare.localauth.mapper.LocalUserMapper;
import com.samplus.smartrecrutare.localauth.repository.LocalUserRepository;
import com.samplus.smartrecrutare.models.PaginaModel;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Orchestrateste administrarea utilizatorilor locali si a rolurilor lor. */
@Service
public class LocalUserAdminService {
    private final LocalUserRepository userRepository;
    private final EmployerService employerService;
    private final PasswordEncoder passwordEncoder;
    private final LocalUserMapper mapper;

    public LocalUserAdminService(
            LocalUserRepository userRepository,
            EmployerService employerService,
            PasswordEncoder passwordEncoder,
            LocalUserMapper mapper
    ) {
        this.userRepository = userRepository;
        this.employerService = employerService;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Transactional
    public LocalUserResponse create(LocalUserCreateRequest request) {
        verificaUnicitate(request.getUsername(), request.getEmail(), null);
        LocalUser user = LocalUser.creare(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRoles()
        );
        userRepository.saveAndFlush(user);
        return mapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public PaginaModel<LocalUserResponse> list(Pageable pageable) {
        var page = userRepository.findAll(pageable);
        List<LocalUserResponse> content = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return new PaginaModel<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public LocalUserResponse getById(Long id) {
        return mapper.toResponse(findById(id));
    }

    @Transactional
    public LocalUserResponse update(Long id, LocalUserUpdateRequest request) {
        LocalUser user = findById(id);
        verificaUnicitate(request.getUsername(), request.getEmail(), id);
        user.inlocuireProfil(request.getUsername(), request.getEmail(), request.getEnabled(), request.getLocked());
        userRepository.flush();
        return mapper.toResponse(user);
    }

    @Transactional
    public LocalUserResponse replaceRoles(Long id, LocalUserRolesRequest request) {
        LocalUser user = findById(id);
        user.inlocuireRoluri(request.getRoles());
        userRepository.flush();
        return mapper.toResponse(user);
    }

    @Transactional
    public void replacePassword(Long id, LocalUserPasswordRequest request) {
        LocalUser user = findById(id);
        user.inlocuireParola(passwordEncoder.encode(request.getPassword()));
        userRepository.flush();
    }

    @Transactional
    public LocalUserResponse assignEmployer(Long id, ManagerEmployerAssignmentRequest request) {
        LocalUser user = findById(id);
        Employer employer = employerService.findEntityById(request.getEmployerId());
        user.atribuieEmployer(employer);
        userRepository.flush();
        return mapper.toResponse(user);
    }

    @Transactional
    public LocalUserResponse removeEmployer(Long id, Long employerId) {
        LocalUser user = findById(id);
        employerService.findEntityById(employerId);
        user.stergeEmployerAdministrat(employerId);
        userRepository.flush();
        return mapper.toResponse(user);
    }

    private LocalUser findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new LocalAuthNotFoundException(id));
    }

    private void verificaUnicitate(String username, String email, Long currentId) {
        boolean usernameExists = currentId == null
                ? userRepository.existsByUsernameIgnoreCase(username)
                : userRepository.existsByUsernameIgnoreCaseAndIdNot(username, currentId);
        if (usernameExists) {
            throw new LocalAuthConflictException("Username-ul local este deja folosit");
        }
        boolean emailExists = currentId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (emailExists) {
            throw new LocalAuthConflictException("Emailul local este deja folosit");
        }
    }
}
