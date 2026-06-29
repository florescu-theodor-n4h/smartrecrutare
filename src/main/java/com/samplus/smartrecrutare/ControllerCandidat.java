package com.samplus.smartrecrutare;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import java.nio.file.*;

@RestController
@RequestMapping("/api/candidati")
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = "*") // pentru mediul DEV
@Schema(description = "Controllerul de candidati. Aici sunt definite metodele")
public class ControllerCandidat {
    private final ServiciuCandidat serviciuCandidat;
    // private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final Logger log = LoggerFactory.getLogger(ControllerCandidat.class);

    protected ControllerCandidat(ServiciuCandidat serviciuCandidat) {
        this.serviciuCandidat = serviciuCandidat;
    }

    @Operation(summary = "Creează un candidat nou",
            description = "Adaugă un candidat nou în baza de date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Candidat creat cu succes",
                    content = @Content(schema = @Schema(implementation = Candidat.class))),
            @ApiResponse(responseCode = "200", description = "Candidat creat cu succes",
                    content = @Content(schema = @Schema(implementation = Candidat.class)))
    })
    @Transactional
    @PostMapping
    public ResponseEntity<Candidat> creare(@RequestBody Candidat candidat) {
        Candidat salvat = serviciuCandidat.creare(candidat);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvat);
    }

    @Transactional
    @DeleteMapping("/{numeCandidate}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rezultatul operației de ștergere"),
            @ApiResponse(responseCode = "400", description = "Parametru invalid")
    })
    public ResponseEntity<Boolean> stergere(@PathVariable String numeCandidate) {
        boolean sters = serviciuCandidat.stergere(numeCandidate);
        return ResponseEntity.ok(sters);
    }

    // -------------------------------------------------------------------------
    // DELETE /api/candidati/id/{id}
    // -------------------------------------------------------------------------

    @Operation(summary = "Șterge un candidat după id",
            description = "Mai sigur și mai performant decât ștergerea după nume.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rezultatul operației de ștergere"),
            @ApiResponse(responseCode = "400", description = "Id invalid")
    })
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Boolean> stergereById(@PathVariable Long id) {
        boolean sters = serviciuCandidat.stergereById(id);
        return ResponseEntity.ok(sters);
    }

    // -------------------------------------------------------------------------
    // PUT /api/candidati/{id}
    // -------------------------------------------------------------------------

    @Operation(summary = "Actualizează un candidat existent",
            description = "Actualizează câmpurile numePrenume, mail, tel. Id-ul din path are prioritate.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidat actualizat",
                    content = @Content(schema = @Schema(implementation = Candidat.class))),
            @ApiResponse(responseCode = "404", description = "Candidat inexistent"),
            @ApiResponse(responseCode = "400", description = "Date invalide")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Candidat> actualizare(@PathVariable Long id,
                                                @RequestBody Candidat dto) {
        try {
            Candidat actualizat = serviciuCandidat.actualizare(id, dto);
            return ResponseEntity.ok(actualizat);
        } catch (EntityNotFoundException e) {
            log.warn("Actualizare esuata — candidat inexistent: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<Collection<Candidat>> getTotiCandidatii() {
        final long start = System.currentTimeMillis();
        Collection<Candidat> candidati = serviciuCandidat.getTotiCandidatii();
        //var cand = List.<Candidat>of();
        final long sf = System.currentTimeMillis();
        log.info("Query GetTotiCandidati() t={}]ms m={}", sf-start,candidati.size());
        return ResponseEntity.ok(candidati);
    }


    // -------------------------------------------------------------------------
    // GET /api/candidati/{id}
    // -------------------------------------------------------------------------

    @Operation(summary = "Caută un candidat după id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidat găsit",
                    content = @Content(schema = @Schema(implementation = Candidat.class))),
            @ApiResponse(responseCode = "404", description = "Candidat inexistent")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Candidat> gasireById(@PathVariable Long id) {
        return serviciuCandidat.gasireById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
