# Backend Architecture

SmartRecrutare is a Spring Boot backend using REST controllers, DTOs, service-layer transactions, JPA repositories, OAuth2/JWT security, and OpenAPI annotations.

## System Diagram

```mermaid
flowchart TB
    subgraph ClientLayer[Client Layer]
        Web[Vue Frontend]
        Swagger[Swagger UI / API Client]
    end

    subgraph EdgeLayer[Application Edge]
        Auth[Authentication Flow]
        Security[Spring Security Filter Chain]
        OpenAPI[OpenAPI Documentation]
    end

    subgraph ApiLayer[REST API Layer]
        JobController[Job Controller]
        EmployerController[Employer Controller]
        AdminController[Admin / Analytics Controllers]
    end

    subgraph BusinessLayer[Business Layer]
        JobService[Job Service]
        EmployerService[Employer Service]
        RolePolicy[Role / Permission Policy]
    end

    subgraph PersistenceLayer[Persistence Layer]
        JobRepository[Job Repository]
        EmployerRepository[Employer Repository]
        UserRepository[User / Role Repository]
    end

    subgraph DataLayer[Data Layer]
        Database[(Relational Database)]
    end

    Web --> Auth
    Swagger --> Security
    Auth --> Security
    Security --> JobController
    Security --> EmployerController
    Security --> AdminController
    JobController --> JobService
    EmployerController --> EmployerService
    AdminController --> RolePolicy
    JobService --> JobRepository
    EmployerService --> EmployerRepository
    RolePolicy --> UserRepository
    JobRepository --> Database
    EmployerRepository --> Database
    UserRepository --> Database
```

`UserRepository` is shown as a future/identity-provider integration boundary. The current backend does not define local user or role persistence.

## Layer Responsibilities

REST controllers:

- Accept HTTP requests.
- Apply route-level and method-level authorization.
- Validate request DTOs.
- Return response DTOs and HTTP status codes.

Services:

- Hold business workflow and authorization-independent rules.
- Use `@Transactional` for writes.
- Use `@Transactional(readOnly = true)` for reads.

Repositories:

- Encapsulate JPA persistence.
- Expose only query methods needed by services.

Security:

- Enforces public/private route boundaries in `SecurityFilterChain`.
- Enforces business permissions through `@PreAuthorize`.
- Converts JWT roles to internal Spring authorities.

## Employer and Job Workflow

```mermaid
flowchart LR
    Admin[Admin / Manager] -->|POST /api/employers| EmployerController
    EmployerController --> EmployerService
    EmployerService --> EmployerRepository
    EmployerRepository --> DB[(Database)]

    Admin -->|POST /api/jobs with employerId| JobController
    JobController --> JobService
    JobService --> EmployerRepository
    JobService --> JobRepository
    JobRepository --> DB

    Guest[Guest / User] -->|GET /api/jobs/active| JobController
```

## Hardening Decisions

- Public browsing is limited to active job listings.
- Administrative employer and job reads require elevated read roles.
- Job and employer writes require admin or manager.
- Deletes are admin-only.
- Safe analytics reads are available to admin, auditor, and governmental users.
- No local production user seeding was added.

## Deferred Improvements

- Fine-grained manager ownership by assigned employer is not implemented because no ownership model exists yet.
- Local user/role management is not implemented because the project currently delegates identity to Auth0/OAuth2.
- Soft delete is not implemented because the current audit base does not define deletion state.
- Database migrations are still deferred because the project currently uses Hibernate auto DDL.
