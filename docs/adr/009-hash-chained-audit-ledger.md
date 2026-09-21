# ADR 009: Hash-Chained Audit Log & Advice Ledger with External Timestamp Anchoring

## Status
Accepted

## Context
Patients and doctors need tamper-evident proof that medical advice, lab records, and consent grants were recorded at specific times and have not been altered or backdated. Public blockchain solutions are expensive, slow, non-private, and legally problematic under DPDP data erasure rights.

## Decision
We implement a lightweight, local-and-server cryptographic ledger:
1. Every advice entry and audit log entry includes `prev_hash` and computes `hash = SHA256(prev_hash + canonical_json(entry))`.
2. Verified doctors sign their advice entries with an ECDSA P-256 signature generated inside the device Keystore.
3. Daily Merkle roots of the audit chain are anchored externally via RFC 3161 Timestamp Authorities (TSA) or OpenTimestamps.

## Consequences
- **Positive**: Cryptographic proof of history and tamper detection without blockchain tokens, gas fees, or privacy leaks.
- **Negative**: Represents digital evidence rather than automated legal admissibility; requires documented chain-verification routines.
