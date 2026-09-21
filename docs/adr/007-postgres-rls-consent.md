# ADR 007: Defense-in-Depth Consent Enforcement via PostgreSQL Row-Level Security (RLS)

## Status
Accepted

## Context
Application-level permission checks can suffer from bugs, missing decorator filters, or race conditions that expose one patient's medical records to unauthorized doctors or users.

## Decision
We enforce consent at the database engine layer using PostgreSQL Row-Level Security (RLS):
1. Application queries execute within transactions where session variables `app.current_user_id` and `app.active_consent_token` are set.
2. PostgreSQL RLS policies evaluate access against active, non-expired, non-revoked rows in the `consents` table.
3. If an application bug omits a filter, PostgreSQL returns zero rows.

## Consequences
- **Positive**: Strict defense-in-depth preventing cross-tenant leakage.
- **Negative**: Requires dedicated integration tests running directly against PostgreSQL to verify RLS policy behavior.
