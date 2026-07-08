# Auth0 QA Practices

This checklist keeps Auth0 workflow tests deterministic and avoids accidental credential use.

## Test Boundaries

- Unit and controller tests must use dummy Auth0 values, not local or production credentials.
- PAR and token endpoint calls should be covered with `MockRestServiceServer`.
- Controller tests should mock `Auth0Service` and verify HTTP/session behavior only.
- Tests must not log `client_secret`, `access_token`, `id_token`, `refresh_token`, private keys, or raw token maps.

## Workflow Coverage

- `GET /auth/login` redirects to the authorize URL created by `Auth0Service`.
- `Auth0Service.createAuthorizeUrl` stores OAuth `state`, `nonce`, and PKCE verifier in session.
- The authorize URL exposes only `client_id` and `request_uri`.
- Callback rejects mismatched OAuth state before any token request.
- Callback rejects missing PKCE verifier before any token request.
- Successful token exchange clears transient OAuth session attributes.
- `GET /auth/me` reports authentication state without returning raw tokens.

## Manual QA

1. Start the backend with Auth0 settings provided through environment variables or a local-only config file.
2. Open `/auth/login` and verify redirect to the configured Auth0 tenant.
3. Complete login and verify the frontend callback receives `login=success`.
4. Call `/auth/me` with the same browser session and verify it returns `authenticated=true`.
5. Confirm `/auth/me` does not include raw token fields.

## Security Rules

- Treat checked-in credentials and private keys as compromised.
- Rotate credentials if they were committed or shared outside the local machine.
- Prefer environment variables or an ignored local profile for Auth0 secrets.
- Keep real Auth0 integration tests opt-in; they should never run in the default Maven test suite.
