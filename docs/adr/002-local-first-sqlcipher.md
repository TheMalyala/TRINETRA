# ADR 002: Local-First Architecture with Room + SQLCipher

## Status
Accepted

## Context
Patients require immediate access to their records during doctor visits, regardless of hospital basement connectivity or cloud outages. Furthermore, health data privacy demands that data resides on the patient's device by default.

## Decision
We adopt a **local-first architecture**:
1. Room + SQLCipher (`net.zetetic:sqlcipher-android`) acts as the single source of truth for patient-private records.
2. The database encryption key is generated at runtime and wrapped by an asymmetric hardware key inside the Android Keystore.
3. Private records sync opportunistically; client-side encrypted backups (Argon2id + AES-256-GCM) prevent data loss.
4. Shared objects (appointments, messages, verified advice) are server-authoritative.

## Consequences
- **Positive**: Zero latency, works completely offline, robust data privacy by design.
- **Negative**: Requires robust conflict resolution (outbox pattern with monotonic versioning).
