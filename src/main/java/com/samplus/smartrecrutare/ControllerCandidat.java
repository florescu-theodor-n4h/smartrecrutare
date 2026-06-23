package com.samplus.smartrecrutare;

import com.samplus.smartrecrutare.Candidat;
import com.samplus.smartrecrutare.DepozitCandidati;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.*;

import javax.naming.ldap.Control;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/candidati")
@Schema(description = "Controllerul de candidati. Aici sunt definite metodele")
public class ControllerCandidat {
    private final DepozitCandidati depCandidati;

    protected ControllerCandidat(DepozitCandidati depozitCandidati) {
        this.depCandidati = depozitCandidati;
    }

    @PostMapping
    public Candidat creare(@RequestBody Candidat candidat) {
        return depCandidati.save(candidat);
    }

    @GetMapping
    public Collection<Candidat> getTotiCandidatii() {
       return depCandidati.findAll();
    }
}
