# SmartRecrutare Employer and Job Workflow

This document describes the backend workflow for employers and jobs after the employer domain was introduced.

## Runtime Architecture

```mermaid
flowchart LR
    Client[Frontend / API Client] --> Security[Spring Security Filter Chain]
    Security --> EmployerController[Employer REST Controller]
    Security --> JobController[Job REST Controller]
    EmployerController --> EmployerService[Employer Service with Transactions]
    JobController --> JobService[Job Service with Transactions]
    EmployerService --> EmployerMapper[Employer DTO Mapper]
    JobService --> JobMapper[Job DTO Mapper]
    EmployerService --> EmployerRepository[Employer Repository]
    JobService --> JobRepository[Job Repository]
    JobService --> EmployerRepository
    EmployerRepository --> DB[(Database)]
    JobRepository --> DB
```

## Domain Relationship

```mermaid
erDiagram
    EMPLOYER ||--o{ JOB : publishes
    EMPLOYER {
        bigint id
        string nume
        string denumire_legala
        string cod_fiscal
        string email_contact
        string telefon_contact
        string website
        string locatie
        string descriere
        string status
        bigint version
        timestamp created_at
        timestamp updated_at
        string created_by
        string updated_by
    }
    JOB {
        bigint id
        string titlu
        string descriere
        string companie
        bigint employer_id
        string locatie
        string salariu
        string tip_contract
        boolean activ
        bigint version
        timestamp created_at
        timestamp updated_at
        string created_by
        string updated_by
    }
```

`Job.companie` remains as a legacy/display field for compatibility. New DTO-based job creation and update require `employerId`.

## API Workflow

1. Admin or manager authenticates through the configured Spring Security/OAuth2 resource server.
2. Admin or manager creates an employer with `POST /api/employers`.
3. Admin or manager publishes a job with `POST /api/employers/{employerId}/jobs`.
   The employer ID comes from the URL, not from the request body, and a manager
   must be assigned to that employer. `POST /api/jobs` with `employerId` remains
   available for backwards compatibility.
4. Users or guests can read public/active jobs according to the security rules configured in the security layer.
5. Auditor or governmental users can inspect records according to the role rules configured in a later security pass.

## Endpoints

Employer API:

- `GET /api/employers`
- `GET /api/employers/{id}`
- `POST /api/employers`
- `PUT /api/employers/{id}`
- `DELETE /api/employers/{id}`
- `POST /api/employers/{employerId}/jobs`

Job API:

- `GET /api/jobs`
- `GET /api/jobs/{id}`
- `POST /api/jobs`
- `PUT /api/jobs/{id}`
- `DELETE /api/jobs/{id}`
- `GET /api/jobs/active`
- `GET /api/jobs/cauta?titlu=java`

### Publicarea unui job de catre angajator

`POST /api/employers/{employerId}/jobs` necesita autentificare. Administratorii
pot publica pentru orice angajator, iar managerii locali doar pentru
angajatorii atribuiti contului lor. Corpul cererii nu contine `employerId`:

```json
{
  "titlu": "Java Developer",
  "descriere": "Dezvoltare API-uri Spring Boot",
  "locatie": "Remote",
  "salariu": "5000-7000 EUR",
  "tipContract": "Full-time",
  "activ": true
}
```

Raspunsul este `201 Created` si include jobul publicat. Campul `companie` este
optional; daca lipseste, API-ul foloseste numele angajatorului.

## Audit

`Employer` and `Job` inherit audit metadata from the existing `AuditableEntity` base class:

- `version`
- `createdAt`
- `updatedAt`
- `createdBy`
- `updatedBy`

No second audit model is introduced.
