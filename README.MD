# SmartRecrutare

Backend Spring Boot pentru managementul angajatorilor, joburilor, candidatilor,
autentificarii locale/OAuth2 si analizelor de potrivire.

## Cerinte

- Java 17 sau mai nou
- Maven 3.9 sau mai nou
- O baza de date configurata pentru profilul ales

Pe Termux:

```sh
pkg update
pkg install openjdk-17 maven ant
```

## Rulare si verificare

```sh
mvn test
mvn package
mvn spring-boot:run
```

Configuratia standard este in `src/main/resources/application.yaml`, iar
configuratia de test in `src/test/resources/application-test.yaml`. Nu adauga
chei, parole sau configuratii locale in Git.

Logurile rotative sunt scrise implicit in
`${java.io.tmpdir}/smartrecrutare`. Pentru un director persistent, seteaza
`logging.file.path` in configuratia locala sau ca proprietate JVM.

Swagger UI este disponibil, dupa pornirea aplicatiei, la:

```text
http://localhost:8080/swagger-ui/index.html
```

## Publicarea joburilor

Un administrator sau manager poate publica un job pentru un angajator:

```text
POST /api/employers/{employerId}/jobs
```

Managerul trebuie sa fie atribuit angajatorului. Cererea nu accepta
`employerId` in corp; astfel, un manager nu poate redirectiona accidental sau
intentionat jobul catre alta companie. Consultati
[`docs/backend-employer-job-workflow.md`](docs/backend-employer-job-workflow.md)
pentru endpointuri, roluri si exemple.

## Documentatie

- `docs/backend-architecture.md` - arhitectura backend
- `docs/backend-employer-job-workflow.md` - flux angajatori si joburi
- `docs/security-rbac-workflow.md` - roluri si autorizare
- `docs/auth0-qa-practices.md` - verificari Auth0

## Contributii

Cititi [`AGENTS.md`](AGENTS.md) inainte de modificari. Orice schimbare de
comportament trebuie sa aiba teste si documentatie aferente.

## CI/CD GitHub

- **Integrare continua**: fiecare push sau pull request spre `master` ruleaza
  `mvn verify` cu Java 17 si pastreaza WAR-ul ca artefact GitHub Actions.
- **Livrare continua**: un tag de forma `v*` (de exemplu `v0.1.0`) construieste
  WAR-ul si il ataseaza automat unui GitHub Release. Nu sunt necesare secrete
  de deployment pentru acest flux.
