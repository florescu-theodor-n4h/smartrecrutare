package com.samplus.smartrecrutare.employer.repository;

import com.samplus.smartrecrutare.employer.domain.Employer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
    boolean existsByCodFiscalIgnoreCase(String codFiscal);
    boolean existsByCodFiscalIgnoreCaseAndIdNot(String codFiscal, Long id);
    Page<Employer> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
