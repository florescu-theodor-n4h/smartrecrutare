package com.samplus.smartrecrutare.localauth.service;

import com.samplus.smartrecrutare.localauth.exception.LocalAuthConflictException;
import com.samplus.smartrecrutare.localauth.repository.LocalUserRepository;
import org.springframework.stereotype.Component;

/**
 * Verifica unicitatea credentialelor publice ale unui utilizator local.
 *
 * <p>Validatorul este separat de fluxurile admin si public ca regula de business sa ramana intr-un
 * singur loc.</p>
 */
@Component
public class LocalUserUniquenessValidator {
    private final LocalUserRepository userRepository;

    public LocalUserUniquenessValidator(LocalUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void verificaUnicitate(String username, String email, Long currentId) {
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
