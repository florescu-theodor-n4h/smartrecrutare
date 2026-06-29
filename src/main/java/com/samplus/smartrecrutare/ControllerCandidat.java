package com.samplus.smartrecrutare;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ILoggerFactory;

import jakarta.annotation.PostConstruct;
import java.nio.file.*;
import java.lang.management.ManagementFactory;
import java.time.Instant;

@RestController
@RequestMapping("/api/candidati")
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = "*") // pentru mediul DEV
@Schema(description = "Controllerul de candidati. Aici sunt definite metodele")
public class ControllerCandidat {
    private final DepozitCandidati depCandidati;
    // private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final Logger log = LoggerFactory.getLogger(ControllerCandidat.class);

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
        //var cand = depCandidati.findAll();
        var cand = List.<Candidat>of();
        final long sf = System.currentTimeMillis();
        log.info("Query GetTotiCandidati() t={}]ms m={}", sf-start,cand.size());
        return cand;
    }

    @PostConstruct
    public void init() {
        System.err.println("🔥🔥🔥 POSTCONSTRUCT SYSERR FIRED");
        System.out.println("🔥🔥🔥 POSTCONSTRUCT SYSOUT FIRED");
        log.error("🔥🔥🔥 LOGBACK POSTCONSTRUCT ERROR FIRED");
        log.info("🔥🔥🔥 LOGBACK POSTCONSTRUCT INFO FIRED");
    }

    @GetMapping("/nuclear-test")
    public String nuclearTest() {

        System.err.println("🚨 SYSERR ENDPOINT HIT");
        System.out.println("🚨 SYSOUT ENDPOINT HIT");

        log.error("🚨 LOG ERROR ENDPOINT HIT");
        log.warn("🚨 LOG WARN ENDPOINT HIT");
        log.info("🚨 LOG INFO ENDPOINT HIT");
        log.debug("🚨 LOG DEBUG ENDPOINT HIT");

        try {
            int x = 1 / 0;
        } catch (Exception e) {
            log.error("💥 EXCEPTION TEST", e);
        }

        return "NUCLEAR OK";
    }

    @PostConstruct
    public void forceFileWrite() {
        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Paths.get("/tmp/direct-java.log"),
                    "🔥 DIRECT JVM WRITE WORKING\n",
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {}
    }

    @PostConstruct
    public void probeLogging() {
        System.err.println("SYSERR: LOGGER FACTORY = " + LoggerFactory.getILoggerFactory().getClass());
    }

    @PostConstruct
    public void nuclearInstrumentation() {

        String header = "\n\n🔥🔥🔥 NUCLEAR INSTRUMENTATION START " + Instant.now() + " 🔥🔥🔥\n";

        // 1. Hard JVM output (bypasses everything)
        System.err.println(header + "SYSERR ACTIVE");
        System.out.println(header + "SYSOUT ACTIVE");

        // 2. SLF4J test
        log.error("🔥 SLF4J ERROR LEVEL ACTIVE");
        log.warn("🔥 SLF4J WARN LEVEL ACTIVE");
        log.info("🔥 SLF4J INFO LEVEL ACTIVE");
        log.debug("🔥 SLF4J DEBUG LEVEL ACTIVE");

        // 3. Logback binding check
        try {
            ILoggerFactory factory = LoggerFactory.getILoggerFactory();
            System.err.println("LOGGER FACTORY CLASS = " + factory.getClass().getName());
        } catch (Exception e) {
            System.err.println("LOGGER FACTORY FAILED: " + e.getMessage());
        }

        // 4. JVM identity dump
        try {
            String jvmInfo = """
                JVM NAME: %s
                JVM VERSION: %s
                PID: %s
                """.formatted(
                    System.getProperty("java.vm.name"),
                    System.getProperty("java.version"),
                    ManagementFactory.getRuntimeMXBean().getName()
            );

            System.err.println(jvmInfo);
        } catch (Exception e) {
            System.err.println("JVM INFO FAILED");
        }

        // 5. File system write test (absolute path bypass)
        try {
            Path p = Paths.get("/tmp/nuclear-instrumentation.log");

            Files.writeString(
                    p,
                    header + "FILE WRITE ACTIVE\n",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            System.err.println("FILE WRITE SUCCESS: " + p);

        } catch (Exception e) {
            System.err.println("FILE WRITE FAILED: " + e.getMessage());
        }

        // 6. Classpath sanity check
        try {
            System.err.println("CLASS = " + getClass().getName());
            System.err.println("CLASS LOADER = " + getClass().getClassLoader());
        } catch (Exception e) {
            System.err.println("CLASS CHECK FAILED");
        }

        // 7. Force exception capture test
        try {
            throw new RuntimeException("🔥 NUCLEAR TEST EXCEPTION");
        } catch (Exception e) {
            log.error("🔥 CAUGHT TEST EXCEPTION", e);
            System.err.println("EXCEPTION STACK TEST COMPLETE");
        }

        System.err.println("🔥🔥🔥 NUCLEAR INSTRUMENTATION END 🔥🔥🔥\n\n");
    }

    // Optional endpoint trigger version
    public void runOnDemand() {
        nuclearInstrumentation();
    }
}
