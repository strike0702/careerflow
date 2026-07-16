# ADR-008: Keycloak-Hosted Self-Registration, Google Login, and Frontend Onboarding Gate

**Status:** Accepted
**Date:** 2026-07

## Context

Previously, only two pre-seeded Keycloak users (`candidate@careerflow.com`, `admin@careerflow.com`) could authenticate. `registrationAllowed` was absent from `realm-export.json` (Keycloak default: disabled), and the frontend had no way to reach a registration page. A completely new user could not create an account.

We need self-service registration, social login via Google, and a way to route first-time candidates to complete their profile before using the dashboard — without weakening the existing JWT/OIDC architecture (see [ADR-001](./ADR-001-use-keycloak-as-identity-provider.md)) or the lazy User/CandidateProfile sync already implemented in `UserService.getOrSyncUser`.

Alternatives considered:

- **Custom registration form in React, posting to Keycloak Admin API** — rejected. Requires exposing admin-level credentials or a service account to the SPA, re-implements password/email-verification logic Keycloak already provides, and contradicts ADR-001.
- **Custom OAuth handling for Google in the SPA (separate from Keycloak)** — rejected. Would issue a second, non-Keycloak-shaped identity token that downstream services don't understand; breaks the single-JWT-validation model.
- **Keycloak-hosted registration + Identity Brokering for Google** — accepted. No new credential handling in the SPA; downstream services keep validating the same Keycloak-issued JWT regardless of how the user authenticated.

## Decision

1. **Enable self-registration in Keycloak** (`registrationAllowed: true`, `registrationEmailAsUsername: true`, `loginWithEmailAllowed: true`, `resetPasswordAllowed: true`, a minimal `passwordPolicy`). Registration UI is Keycloak's own hosted page; the SPA never collects a password.
2. **Auto-assign the `CANDIDATE` role to every new user** via a Keycloak default group (`/candidates`, mapped to realm role `CANDIDATE`) rather than editing the auto-managed `default-roles-<realm>` composite role. Default groups apply uniformly whether the user account was created through registration or through first-time Google login.
3. **Add Google as an Identity Provider** (`identityProviders` entry, `providerId: google`) so "Sign in with Google" appears on Keycloak's hosted login/registration pages. The SPA is unaware of the brokering; it still only ever sees a Keycloak-issued JWT.
4. **Frontend adds a `register()` call** (`keycloak.register()`) next to the existing `login()`/`logout()`, exposed via `AuthProvider`, and a landing screen (`AuthLanding`) with "Sign In" / "Create Account" buttons replaces the previous behavior of immediately force-redirecting every unauthenticated visitor into Keycloak.
5. **User/CandidateProfile lazy sync is unchanged.** `UserService.getOrSyncUser` still creates the `User` row and an empty `CandidateProfile` on the first authenticated call to `/api/v1/users/me`, regardless of whether the user registered via the form or via Google.
6. **Profile completion nudge in the frontend**: when the candidate profile lacks a target role or skill, the dashboard shows a dismissible banner linking to `/profile`. The app is not blocked behind a route gate. No new backend endpoint, field, or migration was introduced — completeness is derived from existing `CandidateProfile` fields.

## Consequences

**Positive:**

- No password handling, email verification, or credential storage added to CareerFlow's own code — all delegated to Keycloak, consistent with ADR-001.
- Google login reuses the existing JWT validation path in every service; zero backend changes required.
- Onboarding nudge is a pure frontend concern with no new database schema, keeping Application/User Service boundaries untouched.
- New users always receive `CANDIDATE`, never `ADMIN`, by construction of the default group mapping.

**Negative:**

- `verifyEmail` is left `false` for local development (no SMTP server in `docker-compose.yml`). Any shared/production deployment must configure SMTP and enable email verification before opening registration publicly.
- The Google `identityProviders` entry in `realm-export.json` ships with placeholder `clientId`/`clientSecret` values (`REPLACE_WITH_GOOGLE_OAUTH_CLIENT_ID`/`_SECRET`) since real Google OAuth credentials cannot be generated on the user's behalf. Google login will not function until these are replaced with real values (ideally configured via the Keycloak Admin Console rather than committed to the repository) or supplied via a locally-modified, untracked copy of the realm file.
- Profile "completeness" is a frontend-only judgment call (target roles + at least one skill). If a future service needs to enforce this server-side, the same rule should be duplicated in User Service or promoted into a shared definition.

**Follow-up:**

- Configure SMTP + `verifyEmail: true` before any non-local deployment.
- Replace Google identity provider placeholder credentials with real ones from Google Cloud Console.
- Revisit `@PreAuthorize` role enforcement (tracked separately, see [technical-debt.md](../technical-debt.md)) now that role assignment happens automatically at registration time.
