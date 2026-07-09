package com.samplus.smartrecrutare.localauth.domain;

import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.security.AuditableEntity;
import com.samplus.smartrecrutare.security.RolAplicatie;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/** Utilizator local stocat in baza de date, fara dependinta de Auth0. */
@Entity
@Table(
        name = "local_auth_users",
        indexes = {
                @Index(name = "idx_local_user_username", columnList = "username", unique = true),
                @Index(name = "idx_local_user_email", columnList = "email", unique = true)
        }
)
public class LocalUser extends AuditableEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Getter
    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Getter
    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Getter
    @Column(nullable = false)
    private boolean enabled = true;

    @Getter
    @Column(nullable = false)
    private boolean locked;

    @Getter
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "local_auth_user_roles",
            joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_local_role_user"))
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 40)
    private Set<RolAplicatie> roles = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "local_auth_manager_employers",
            joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_local_manager_user")),
            inverseJoinColumns = @JoinColumn(name = "employer_id", foreignKey = @ForeignKey(name = "fk_local_manager_employer"))
    )
    private Set<Employer> managedEmployers = new LinkedHashSet<>();

    protected LocalUser() {
        // Constructor necesar pentru JPA.
    }

    public static LocalUser creare(String username, String email, String passwordHash, Set<RolAplicatie> roles) {
        LocalUser user = new LocalUser();
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.roles = new LinkedHashSet<>(roles);
        return user;
    }

    public void inlocuireProfil(String username, String email, boolean enabled, boolean locked) {
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.locked = locked;
    }

    public void inlocuireParola(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void inlocuireRoluri(Set<RolAplicatie> roles) {
        this.roles = new LinkedHashSet<>(roles);
    }

    public void atribuieEmployer(Employer employer) {
        this.managedEmployers.add(employer);
    }

    public void stergeEmployerAdministrat(Long employerId) {
        this.managedEmployers.removeIf(employer -> employer.getId() != null && employer.getId().equals(employerId));
    }

    public void marcheazaLogin() {
        this.lastLoginAt = Instant.now();
    }

    public Set<RolAplicatie> getRoles() { return Set.copyOf(roles); }
    public Set<Employer> getManagedEmployers() { return Set.copyOf(managedEmployers); }
    public boolean administreazaEmployer(Long employerId) {
        return employerId != null && managedEmployers.stream()
                .anyMatch(employer -> employer.getId() != null && employer.getId().equals(employerId));
    }
}
