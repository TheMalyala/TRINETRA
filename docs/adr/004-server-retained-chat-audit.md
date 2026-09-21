# ADR 004: Server-Retained and Encrypted-at-Rest Chat (Not E2EE)

## Status
Accepted

## Context
A primary pillar of Trinetra is **clinical accountability**: patients and practitioners need an immutable, auditable record of who advised what and when. End-to-end encryption (E2EE) prevents the platform from auditing advice, promoting chat advice into the Advice Ledger asynchronously, enforcing consent revocations server-side, or running emergency red-flag safety nets.

## Decision
Chat is transmitted over TLS 1.3 and stored on the server with envelope encryption at rest. Chat is non-deletable (immutable append-only history). To safeguard privacy:
1. Message payloads use AES-256-GCM envelope encryption with per-tenant DEKs wrapped by KMS.
2. PostgreSQL Row-Level Security (RLS) restricts message access strictly to authenticated conversation participants with active consent.
3. Attachments are shared as consent-scoped reference tokens rather than file duplications.

## Consequences
- **Positive**: Enables advice ledger promotion, tamper-evident auditability, server-side guardrails, and compliance with telemedicine record retention norms.
- **Negative**: The server operator has technical custody of encrypted data; requires strict operator auditing and DPDP-compliant data retention controls.
