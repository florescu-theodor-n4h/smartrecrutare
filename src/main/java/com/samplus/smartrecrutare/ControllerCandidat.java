package com.samplus.smartrecrutare;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/candidati")
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = "*") // pentru mediul DEV
@Schema(description = "Controllerul de candidati. Aici sunt definite metodele")
public class ControllerCandidat {
    private final DepozitCandidati depCandidati;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected ControllerCandidat(DepozitCandidati depozitCandidati) {
        this.depCandidati = depozitCandidati;
    }

    @Operation(summary = "Creează un candidat nou",
            description = "Adaugă un candidat nou în baza de date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidat creat cu succes",
                    content = @Content(schema = @Schema(implementation = Candidat.class)))
    })
    @PostMapping
    public Candidat creare(@RequestBody Candidat candidat) {
        return depCandidati.save(candidat);
    }

    @DeleteMapping("/{numeCandidate}")
    public Boolean stergere(@PathVariable String numeCandidate) {
        log.info("S-a sters un candidat: {}", numeCandidate);
        final Optional<Candidat> candidatOpt = depCandidati.findByNumePrenume(numeCandidate);
        if (candidatOpt.isPresent()) {
            depCandidati.delete(candidatOpt.get());
            return true;
        }

        return false;
    }

    @PutMapping("/{id}")
    public Candidat update(@PathVariable Long id, @RequestBody Candidat candidat) {
        return depCandidati.findById(id)
                .map(existing -> {
                    existing.setNumePrenume(candidat.getNumePrenume());
                    existing.setMail(candidat.getMail());
                    existing.setTel(candidat.getTel());
                    // add other fields here

                    return depCandidati.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Candidat inexistent"));
    }

    @GetMapping
    public Collection<Candidat> getTotiCandidatii() {
        final long start = System.currentTimeMillis();
        var cand = depCandidati.findAll();
        final long sf = System.currentTimeMillis();
        log.info("Query GetTotiCandidati() t={}]ms m={}", sf-start,cand.size());
        return cand;
    }
}
