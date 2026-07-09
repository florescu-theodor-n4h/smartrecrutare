package com.samplus.smartrecrutare;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ServiciuCandidatTest {

    private final DepozitCandidati depozitCandidati = mock(DepozitCandidati.class);
    private final ServiciuCandidat serviciu = new ServiciuCandidat(depozitCandidati);

    @Test
    void creareResetsClientIdAndReturnsPersistedCandidate() {
        Candidat candidat = candidat(99L, "Ion Popescu", "ion@example.com", "0712345678");
        Candidat salvat = candidat(1L, "Ion Popescu", "ion@example.com", "0712345678");
        when(depozitCandidati.save(candidat)).thenReturn(salvat);

        Candidat rezultat = serviciu.creare(candidat);

        assertThat(candidat.getId()).isNull();
        assertThat(rezultat).isSameAs(salvat);
        verify(depozitCandidati).save(candidat);
    }

    @Test
    void creareRejectsNullCandidate() {
        assertThatThrownBy(() -> serviciu.creare(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
        verifyNoInteractions(depozitCandidati);
    }

    @Test
    void stergereByNameDeletesExistingCandidate() {
        Candidat candidat = candidat(1L, "Ion Popescu", "ion@example.com", "0712345678");
        Set<Candidat> candidati = Set.of(candidat);
        when(depozitCandidati.findByNumePrenume("Ion Popescu")).thenReturn(Set.of(candidat));

        assertThat(serviciu.stergere("Ion Popescu")).isTrue();
        // se sterge doar prin set, pot fi mai multi candidati. candidatul este unic prin
        // ID-ul lui.
        verify(depozitCandidati).deleteAll(candidati);
    }

    @Test
    void stergereByNameIsIdempotentAndRejectsBlankNames() {
        when(depozitCandidati.findByNumePrenume("Necunoscut")).thenReturn(Set.of());

        assertThat(serviciu.stergere("Necunoscut")).isFalse();
        assertThatThrownBy(() -> serviciu.stergere(" "))
                .isInstanceOf(IllegalArgumentException.class);
        verify(depozitCandidati, never()).delete(org.mockito.ArgumentMatchers.any(Candidat.class));
    }

    @Test
    void stergereByIdDeletesExistingCandidateAndIgnoresMissingCandidate() {
        when(depozitCandidati.existsById(1L)).thenReturn(true);
        when(depozitCandidati.existsById(2L)).thenReturn(false);

        assertThat(serviciu.stergereById(1L)).isTrue();
        assertThat(serviciu.stergereById(2L)).isFalse();

        verify(depozitCandidati).deleteById(1L);
        verify(depozitCandidati, never()).deleteById(2L);
    }

    @Test
    void actualizareAppliesOnlyProvidedFields() {
        Candidat existent = candidat(7L, "Ion Popescu", "ion@example.com", "0712345678");
        Candidat dto = candidat(null, "Ion Popescu Actualizat", null, null);
        when(depozitCandidati.findById(7L)).thenReturn(Optional.of(existent));
        when(depozitCandidati.save(existent)).thenReturn(existent);

        Candidat rezultat = serviciu.actualizare(7L, dto);

        assertThat(rezultat).isSameAs(existent);
        assertThat(existent.getId()).isEqualTo(7L);
        assertThat(existent.getNumePrenume()).isEqualTo("Ion Popescu Actualizat");
        assertThat(existent.getMail()).isEqualTo("ion@example.com");
        assertThat(existent.getTel()).isEqualTo("0712345678");
        verify(depozitCandidati).save(existent);
    }

    @Test
    void actualizareRejectsInvalidArgumentsAndMissingCandidate() {
        Candidat dto = new Candidat();

        assertThatThrownBy(() -> serviciu.actualizare(null, dto))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> serviciu.actualizare(1L, null))
                .isInstanceOf(IllegalArgumentException.class);

        when(depozitCandidati.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> serviciu.actualizare(404L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("404");
        verify(depozitCandidati, never()).save(dto);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Candidat candidat = candidat(1L, "Ion Popescu", "ion@example.com", "0712345678");
        when(depozitCandidati.findAll()).thenReturn(List.of(candidat));
        when(depozitCandidati.findById(1L)).thenReturn(Optional.of(candidat));
        when(depozitCandidati.findByNumePrenume("Ion Popescu")).thenReturn(Set.of(candidat));

        assertThat(serviciu.getTotiCandidatii()).containsExactly(candidat);
        assertThat(serviciu.gasireById(1L)).contains(candidat);
        assertThat(serviciu.gasireByNumePrenume("Ion Popescu")).contains(candidat);
    }

    @Test
    void queryAndDeleteMethodsRejectNullOrBlankCriteria() {
        assertThatThrownBy(() -> serviciu.stergereById(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> serviciu.gasireById(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> serviciu.gasireByNumePrenume(" "))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(depozitCandidati);
    }

    private Candidat candidat(Long id, String nume, String mail, String telefon) {
        Candidat candidat = new Candidat();
        candidat.setId(id);
        candidat.setNumePrenume(nume);
        candidat.setMail(mail);
        candidat.setTel(telefon);
        return candidat;
    }
}
