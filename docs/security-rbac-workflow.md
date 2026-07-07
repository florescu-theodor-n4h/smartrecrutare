# Security RBAC Workflow

This document describes the role-based access control currently implemented in SmartRecrutare.

## Authentication Model

SmartRecrutare uses the existing Spring Security OAuth2 resource-server configuration. Requests are authenticated with JWT bearer tokens. No local production users, plaintext passwords, or hardcoded admin accounts were added.

The JWT converter accepts authorities from the default `scope` / `scp` handling and normalizes common role claims:

- `roles`
- `authorities`
- `https://smartrecrutare/roles`

Role labels such as `admin`, `manager`, and `governmental-user` are converted to Spring-compatible authorities:

- `ROLE_ADMIN`
- `ROLE_MANAGER`
- `ROLE_AUDITOR`
- `ROLE_GOVERNMENTAL_USER`
- `ROLE_USER`

Guests are represented by unauthenticated anonymous access. The application does not currently persist a `ROLE_GUEST`.

## Role Matrix

| Capability | Guest | User | Governmental User | Auditor | Manager | Admin |
| --- | --- | --- | --- | --- | --- | --- |
| Read active jobs through `/api/jobs/active` | Yes | Yes | Yes | Yes | Yes | Yes |
| Read full job administration endpoints | No | No | Yes | Yes | Yes | Yes |
| Create/update jobs | No | No | No | No | Yes | Yes |
| Delete jobs | No | No | No | No | No | Yes |
| Read employers | No | No | Yes | Yes | Yes | Yes |
| Create/update employers | No | No | No | No | Yes | Yes |
| Delete employers | No | No | No | No | No | Yes |
| Read safe admin analytics | No | No | Yes | Yes | No | Yes |
| Run analytics jobs / write admin analytics | No | No | No | No | No | Yes |

## Endpoint Policy

Employer endpoints:

- `GET /api/employers`: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_AUDITOR`, `ROLE_GOVERNMENTAL_USER`
- `GET /api/employers/{id}`: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_AUDITOR`, `ROLE_GOVERNMENTAL_USER`
- `POST /api/employers`: `ROLE_ADMIN`, `ROLE_MANAGER`
- `PUT /api/employers/{id}`: `ROLE_ADMIN`, `ROLE_MANAGER`
- `DELETE /api/employers/{id}`: `ROLE_ADMIN`

Job endpoints:

- `GET /api/jobs/active`: public/guest access
- `GET /api/jobs`: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_AUDITOR`, `ROLE_GOVERNMENTAL_USER`
- `GET /api/jobs/{id}`: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_AUDITOR`, `ROLE_GOVERNMENTAL_USER`
- `GET /api/jobs/cauta`: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_AUDITOR`, `ROLE_GOVERNMENTAL_USER`
- `POST /api/jobs`: `ROLE_ADMIN`, `ROLE_MANAGER`
- `PUT /api/jobs/{id}`: `ROLE_ADMIN`, `ROLE_MANAGER`
- `DELETE /api/jobs/{id}`: `ROLE_ADMIN`

Analytics/admin endpoints:

- Safe read endpoints under `/api/admin/analytics`: `ROLE_ADMIN`, `ROLE_AUDITOR`, `ROLE_GOVERNMENTAL_USER`
- Analytics commands and write operations: `ROLE_ADMIN`
- Candidate profile administration remains restricted to the existing admin/manager policy because it can expose candidate-related data.

Auth endpoints:

- `/auth/login`, `/auth/callback`, and `/auth/me` keep their existing public behavior.

## Request Workflow

```mermaid
sequenceDiagram
    participant Client as Client
    participant Security as Spring Security
    participant Controller as REST Controller
    participant Service as Transactional Service
    participant Repository as JPA Repository
    participant DB as Database

    Client->>Security: HTTP request with session/JWT
    Security->>Security: Authenticate user
    Security->>Security: Check role/authority
    Security->>Controller: Authorized request
    Controller->>Controller: Validate DTO
    Controller->>Service: Call business method
    Service->>Service: Start transaction
    Service->>Repository: Load/save entity
    Repository->>DB: SQL operation
    DB-->>Repository: Result
    Repository-->>Service: Entity
    Service-->>Controller: Response DTO
    Controller-->>Client: HTTP response
```

## Manual Verification

Use Swagger UI or an HTTP client with bearer tokens that contain compatible roles.

Admin:

- Create employer: `POST /api/employers`
- Update employer: `PUT /api/employers/{id}`
- Delete employer without jobs: `DELETE /api/employers/{id}`
- Create/update/delete jobs: `POST`, `PUT`, `DELETE /api/jobs`

Manager:

- Create employer/job and update them.
- Verify `DELETE /api/employers/{id}` and `DELETE /api/jobs/{id}` return `403 Forbidden`.

Auditor:

- Read `/api/employers`, `/api/jobs`, and `/api/admin/analytics/dashboard`.
- Verify create/update/delete requests return `403 Forbidden`.

Governmental user:

- Read `/api/employers`, `/api/jobs`, and safe admin analytics.
- Verify create/update/delete requests return `403 Forbidden`.

Normal user:

- Read `/api/jobs/active`.
- Verify `/api/employers`, `/api/jobs`, and `/api/admin/**` return `403 Forbidden`.

Guest:

- Read `/api/jobs/active`.
- Verify private endpoints return `401 Unauthorized`.

## Admin Provisioning

No production admin account is seeded by the backend. Create admin users in the configured identity provider, then include `admin` or `ROLE_ADMIN` in the JWT role claims consumed by the application.
