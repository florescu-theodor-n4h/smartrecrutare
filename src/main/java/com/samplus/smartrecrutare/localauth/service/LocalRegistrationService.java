package com.samplus.smartrecrutare.localauth.service;

import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import com.samplus.smartrecrutare.localauth.dto.LocalRegistrationRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalUserResponse;
import com.samplus.smartrecrutare.localauth.mapper.LocalUserMapper;
import com.samplus.smartrecrutare.localauth.repository.LocalUserRepository;
import com.samplus.smartrecrutare.security.RolAplicatie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Creeaza conturi locale prin gateway-ul public de inregistrare.
 *
 * <p>Serviciul nu accepta roluri de la client si emite doar conturi cu rolul {@link RolAplicatie#USER}.</p>
 */
@Service
public class LocalRegistrationService {
    private final LocalUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocalUserMapper mapper;
    private final LocalUserUniquenessValidator uniquenessValidator;

    public LocalRegistrationService(
            LocalUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LocalUserMapper mapper,
            LocalUserUniquenessValidator uniquenessValidator
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.uniquenessValidator = uniquenessValidator;
    }

    @Transactional
    public LocalUserResponse register(LocalRegistrationRequest request) {
        uniquenessValidator.verificaUnicitate(request.getUsername(), request.getEmail(), null);
        LocalUser user = LocalUser.creare(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Set.of(RolAplicatie.USER)
        );
        userRepository.saveAndFlush(user);
        return mapper.toResponse(user);
    }
}
