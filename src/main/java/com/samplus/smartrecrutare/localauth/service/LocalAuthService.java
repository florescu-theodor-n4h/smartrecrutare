package com.samplus.smartrecrutare.localauth.service;

import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import com.samplus.smartrecrutare.localauth.dto.LocalLoginRequest;
import com.samplus.smartrecrutare.localauth.dto.LocalLoginResponse;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthBadCredentialsException;
import com.samplus.smartrecrutare.localauth.exception.LocalAuthDisabledException;
import com.samplus.smartrecrutare.localauth.mapper.LocalUserMapper;
import com.samplus.smartrecrutare.localauth.repository.LocalUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestioneaza autentificarea credentialelor locale si emiterea tokenurilor. */
@Service
public class LocalAuthService {
    private final LocalUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocalAuthTokenService tokenService;
    private final LocalUserMapper mapper;

    public LocalAuthService(
            LocalUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LocalAuthTokenService tokenService,
            LocalUserMapper mapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.mapper = mapper;
    }

    @Transactional
    public LocalLoginResponse login(LocalLoginRequest request) {
        if (!tokenService.isEnabled()) {
            throw new LocalAuthDisabledException();
        }
        LocalUser user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(LocalAuthBadCredentialsException::new);
        if (!user.isEnabled() || user.isLocked() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new LocalAuthBadCredentialsException();
        }
        user.marcheazaLogin();
        LocalAuthTokenService.TokenData token = tokenService.createToken(user);
        return new LocalLoginResponse("Bearer", token.token(), token.expiresAt(), mapper.toResponse(user));
    }
}
