package com.samplus.smartrecrutare;

import com.samplus.smartrecrutare.employer.domain.Employer;
import com.samplus.smartrecrutare.employer.domain.EmployerStatus;
import com.samplus.smartrecrutare.employer.exception.EmployerNotFoundException;
import com.samplus.smartrecrutare.employer.mapper.EmployerMapper;
import com.samplus.smartrecrutare.employer.repository.EmployerRepository;
import com.samplus.smartrecrutare.job.dto.JobCreateRequest;
import com.samplus.smartrecrutare.job.dto.JobResponse;
import com.samplus.smartrecrutare.job.mapper.JobMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ServiciuJoburiTest {

    private final DepozitJoburi depozitJoburi = mock(DepozitJoburi.class);
    private final EmployerRepository employerRepository = mock(EmployerRepository.class);
    private final JobMapper jobMapper = new JobMapper(new EmployerMapper());
    private final ServiciuJoburi serviciu = new ServiciuJoburi(depozitJoburi, employerRepository, jobMapper);

    @Test
    void creareResetsClientIdAndReturnsPersistedJob() {
        Job job = job(99L, "Java Developer", "Samplus", true);
        Job salvat = job(1L, "Java Developer", "Samplus", true);
        when(depozitJoburi.save(job)).thenReturn(salvat);

        Job rezultat = serviciu.creare(job);

        assertThat(job.getId()).isNull();
        assertThat(rezultat).isSameAs(salvat);
        verify(depozitJoburi).save(job);
    }

    @Test
    void creareRejectsNullAndMissingRequiredFields() {
        assertThatThrownBy(() -> serviciu.creare(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");

        Job faraTitlu = job(null, " ", "Samplus", true);
        assertThatThrownBy(() -> serviciu.creare(faraTitlu))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Titlul");

        Job faraCompanie = job(null, "Java Developer", " ", true);
        assertThatThrownBy(() -> serviciu.creare(faraCompanie))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Compania");

        verifyNoInteractions(depozitJoburi);
    }

    @Test
    void actualizareAppliesOnlyProvidedFieldsAndPreservesIdentity() {
        Job existent = job(7L, "Java Developer", "Samplus", true);
        existent.setDescriere("Descriere veche");
        existent.setLocatie("Bucuresti");

        Job dto = new Job();
        dto.setTitlu("Senior Java Developer");
        dto.setActiv(false);

        when(depozitJoburi.findById(7L)).thenReturn(Optional.of(existent));
        when(depozitJoburi.save(existent)).thenReturn(existent);

        Job rezultat = serviciu.actualizare(7L, dto);

        assertThat(rezultat).isSameAs(existent);
        assertThat(existent.getTitlu()).isEqualTo("Senior Java Developer");
        assertThat(existent.getDescriere()).isEqualTo("Descriere veche");
        assertThat(existent.getCompanie()).isEqualTo("Samplus");
        assertThat(existent.getLocatie()).isEqualTo("Bucuresti");
        assertThat(existent.isActiv()).isFalse();
        assertThat(existent.getId()).isEqualTo(7L);
        verify(depozitJoburi).save(existent);
    }

    @Test
    void creareDinRequestLinksExistingEmployerAndCopiesDisplayCompany() {
        Employer employer = employer();
        JobCreateRequest request = new JobCreateRequest(
                "Java Developer",
                "Backend",
                10L,
                null,
                "Bucuresti",
                "5000 EUR",
                "Full-time",
                true
        );

        when(employerRepository.findById(10L)).thenReturn(Optional.of(employer));
        when(depozitJoburi.saveAndFlush(org.mockito.ArgumentMatchers.any(Job.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = serviciu.creareDinRequest(request);

        assertThat(response.getTitlu()).isEqualTo("Java Developer");
        assertThat(response.getCompanie()).isEqualTo("Samplus");
        assertThat(response.getEmployer().getNume()).isEqualTo("Samplus");
    }

    @Test
    void creareDinRequestFailsForMissingEmployer() {
        JobCreateRequest request = new JobCreateRequest(
                "Java Developer",
                "Backend",
                404L,
                null,
                "Bucuresti",
                null,
                "Full-time",
                true
        );
        when(employerRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviciu.creareDinRequest(request))
                .isInstanceOf(EmployerNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void actualizareRejectsInvalidArgumentsAndMissingJob() {
        Job dto = new Job();

        assertThatThrownBy(() -> serviciu.actualizare(null, dto))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> serviciu.actualizare(1L, null))
                .isInstanceOf(IllegalArgumentException.class);

        when(depozitJoburi.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> serviciu.actualizare(404L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("404");
        verify(depozitJoburi, never()).save(dto);
    }

    @Test
    void stergereDeletesExistingJobAndIsIdempotentForMissingJob() {
        when(depozitJoburi.existsById(5L)).thenReturn(true);
        when(depozitJoburi.existsById(6L)).thenReturn(false);

        assertThat(serviciu.stergere(5L)).isTrue();
        assertThat(serviciu.stergere(6L)).isFalse();

        verify(depozitJoburi).deleteById(5L);
        verify(depozitJoburi, never()).deleteById(6L);
    }

    @Test
    void stergereRejectsNullId() {
        assertThatThrownBy(() -> serviciu.stergere(null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(depozitJoburi);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Job activ = job(1L, "Java Developer", "Samplus", true);
        List<Job> toate = List.of(activ);
        when(depozitJoburi.findAll()).thenReturn(toate);
        when(depozitJoburi.findByActivTrue()).thenReturn(toate);
        when(depozitJoburi.findByTitluContainingIgnoreCase("java")).thenReturn(toate);
        when(depozitJoburi.findById(1L)).thenReturn(Optional.of(activ));

        assertThat(serviciu.getTateJoburile()).containsExactly(activ);
        assertThat(serviciu.getJoburiActive()).containsExactly(activ);
        assertThat(serviciu.cautareDupaTitlu("java")).containsExactly(activ);
        assertThat(serviciu.gasireById(1L)).contains(activ);
    }

    @Test
    void queryMethodsRejectBlankOrNullCriteria() {
        assertThatThrownBy(() -> serviciu.cautareDupaTitlu("  "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> serviciu.gasireById(null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(depozitJoburi);
    }

    private Job job(Long id, String titlu, String companie, boolean activ) {
        Job job = new Job();
        job.setId(id);
        job.setTitlu(titlu);
        job.setCompanie(companie);
        job.setTipContract("Full-time");
        job.setActiv(activ);
        return job;
    }

    private Employer employer() {
        return Employer.creare(
                "Samplus",
                "Samplus SRL",
                "RO12345678",
                "contact@samplus.ro",
                "0712345678",
                "https://samplus.ro",
                "Bucuresti",
                "Companie IT",
                EmployerStatus.ACTIV
        );
    }
}
