package com.samplus.smartrecrutare.localauth.repository;

import com.samplus.smartrecrutare.localauth.domain.LocalUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repository JPA pentru utilizatorii LocalAuth si relatiile lor de roluri. */
public interface LocalUserRepository extends JpaRepository<LocalUser, Long> {
    @EntityGraph(attributePaths = {"roles", "managedEmployers"})
    Optional<LocalUser> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
