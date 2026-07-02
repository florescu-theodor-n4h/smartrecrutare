package com.samplus.smartrecrutare;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Controller REST thin pentru resursa {@code /api/jobs}.
 *
 * <p>Acoperă exact suprafața de API consumată de frontend-ul TypeScript:</p>
 * <pre>
 *   GET    /api/jobs         → getJobs()
 *   GET    /api/jobs/{id}    → getJob(id)
 *   POST   /api/jobs         → createJob(payload)
 *   PUT    /api/jobs/{id}    → updateJob(id, payload)
 *   DELETE /api/jobs/{id}    → deleteJob(id)
 * </pre>
 *
 * <p>Controllerul nu conține logică de business. Toată logica este delegată
 * către {@link ServiciuJoburi}. Responsabilitatea sa este exclusiv traducerea
 * HTTP ↔ Java: mapping-uri, status codes, excepții → răspunsuri HTTP.</p>
 */
@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*") // DEV only — restrict în producție cu origini explicite
@Tag(name = "Joburi", description = "CRUD complet pentru posturile de muncă disponibile")
public class ControllerJoburi {
    private static final Logger log = LoggerFactory.getLogger(ControllerJoburi.class);
    private final ServiciuJoburi serviciuJoburi;

    public ControllerJoburi(ServiciuJoburi serviciuJoburi) {
        this.serviciuJoburi = serviciuJoburi;
    }

    // -------------------------------------------------------------------------
    // GET /api/jobs
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Obține toate joburile",
            description = "Returnează lista completă a joburilor (active și inactive)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listă joburi returnată cu succes",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Job.class))))
    })
    @GetMapping
    public ResponseEntity<Collection<Job>> getJobs() {
        Collection<Job> joburi = serviciuJoburi.getTateJoburile();
        return ResponseEntity.ok(joburi);
    }

    // -------------------------------------------------------------------------
    // GET /api/jobs/{id}
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Obține un job după ID",
            description = "Returnează jobul cu id-ul specificat sau 404 dacă nu există."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job găsit",
                    content = @Content(schema = @Schema(implementation = Job.class))),
            @ApiResponse(responseCode = "404", description = "Job inexistent"),
            @ApiResponse(responseCode = "400", description = "ID invalid")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(
            @Parameter(description = "ID-ul jobului", required = true)
            @PathVariable Long id) {

        return serviciuJoburi.gasireById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Job inexistent la GET: id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // -------------------------------------------------------------------------
    // POST /api/jobs
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Creează un job nou",
            description = "Adaugă un post de muncă nou în sistem. Id-ul din body este ignorat — " +
                    "va fi generat automat. Câmpurile obligatorii: titlu, companie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Job creat cu succes",
                    content = @Content(schema = @Schema(implementation = Job.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide (titlu sau companie lipsă)")
    })
    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job payload) {
        try {
            Job salvat = serviciuJoburi.creare(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvat);
        } catch (IllegalArgumentException e) {
            log.warn("Creare job esuata — date invalide: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/jobs/{id}
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Actualizează un job existent",
            description = "Actualizează parțial câmpurile jobului identificat prin id. " +
                    "Câmpurile null din payload sunt ignorate (nu suprascriu datele existente). " +
                    "Câmpurile de audit și id-ul nu pot fi modificate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job actualizat cu succes",
                    content = @Content(schema = @Schema(implementation = Job.class))),
            @ApiResponse(responseCode = "404", description = "Job inexistent"),
            @ApiResponse(responseCode = "400", description = "Date invalide sau ID null")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(
            @Parameter(description = "ID-ul jobului de actualizat", required = true)
            @PathVariable Long id,
            @RequestBody Job payload) {

        try {
            Job actualizat = serviciuJoburi.actualizare(id, payload);
            return ResponseEntity.ok(actualizat);
        } catch (EntityNotFoundException e) {
            log.warn("Actualizare job esuata — inexistent: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Actualizare job esuata — date invalide: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/jobs/{id}
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Șterge un job după ID",
            description = "Returnează true dacă jobul a fost găsit și șters, false dacă nu există. " +
                    "Operația este idempotentă — nu aruncă eroare dacă jobul lipsește."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rezultatul operației (true/false)"),
            @ApiResponse(responseCode = "400", description = "ID invalid")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteJob(
            @Parameter(description = "ID-ul jobului de șters", required = true)
            @PathVariable Long id) {

        boolean sters = serviciuJoburi.stergere(id);
        return ResponseEntity.ok(sters);
    }

    // -------------------------------------------------------------------------
    // GET /api/jobs/active  (endpoint bonus — util pentru frontend)
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Obține toate joburile active",
            description = "Shortcut pentru listarea exclusivă a joburilor cu activ=true."
    )
    @ApiResponse(responseCode = "200", description = "Listă joburi active",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Job.class))))
    @GetMapping("/active")
    public ResponseEntity<Collection<Job>> getJoburiActive() {
        return ResponseEntity.ok(serviciuJoburi.getJoburiActive());
    }

    // -------------------------------------------------------------------------
    // GET /api/jobs/cauta?titlu=...  (endpoint bonus — util pentru search)
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Caută joburi după titlu",
            description = "Returnează joburile al căror titlu conține fragmentul dat (case-insensitive)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rezultate căutare",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Job.class)))),
            @ApiResponse(responseCode = "400", description = "Parametrul titlu lipsește sau este gol")
    })
    @GetMapping("/cauta")
    public ResponseEntity<?> cautaDupaTitlu(
            @Parameter(description = "Fragment de text căutat în titlul jobului", required = true)
            @RequestParam String titlu) {

        try {
            return ResponseEntity.ok(serviciuJoburi.cautareDupaTitlu(titlu));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}