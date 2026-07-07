package com.samplus.smartrecrutare.localauth.service;

import com.samplus.smartrecrutare.DepozitJoburi;
import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import com.samplus.smartrecrutare.localauth.repository.LocalUserRepository;
import com.samplus.smartrecrutare.security.RolAplicatie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aplica regulile de proprietate pentru managerii locali. */
@Service("localAuthorizationService")
public class LocalAuthorizationService {
    private final LocalUserRepository userRepository;
    private final DepozitJoburi depozitJoburi;

    public LocalAuthorizationService(LocalUserRepository userRepository, DepozitJoburi depozitJoburi) {
        this.userRepository = userRepository;
        this.depozitJoburi = depozitJoburi;
    }

    @Transactional(readOnly = true)
    public boolean canManageEmployer(Long employerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (hasAuthority(auth, RolAplicatie.ADMIN.getAutoritate())) {
            return true;
        }
        if (!hasAuthority(auth, RolAplicatie.MANAGER.getAutoritate())) {
            return false;
        }
        return userRepository.findByUsernameIgnoreCase(auth.getName())
                .map(user -> user.administreazaEmployer(employerId))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean canManageJob(Long jobId) {
        Long employerId = depozitJoburi.findEmployerIdByJobId(jobId).orElse(null);
        return employerId != null && canManageEmployer(employerId);
    }

    @Transactional
    public void assignCreatedEmployerIfLocalManager(Employer employer) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || hasAuthority(auth, RolAplicatie.ADMIN.getAutoritate())
                || !hasAuthority(auth, RolAplicatie.MANAGER.getAutoritate())) {
            return;
        }
        userRepository.findByUsernameIgnoreCase(auth.getName()).ifPresent(user -> {
            user.atribuieEmployer(employer);
            userRepository.flush();
        });
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }
}
