package com.samplus.smartrecrutare;

import com.samplus.smartrecrutare.Candidat;
import com.samplus.smartrecrutare.DepozitCandidati;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.naming.ldap.Control;
import java.util.Collection;
import java.util.List;

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

    @PostMapping
    public Candidat creare(@RequestBody Candidat candidat) {
        return depCandidati.save(candidat);
    }

    @DeleteMapping("/{numeCandidate}")
    public Boolean stergere(@PathVariable String numeCandidate) {
        return true;
    }

    @PutMapping("/{id}")
    public Candidat update(@PathVariable Long id, @RequestBody Candidat candidat) {
        return depCandidati.findById(id)
                .map(existing -> {
                    candidat.setId(id);
                    return depCandidati.save(candidat);
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
