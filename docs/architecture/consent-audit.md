# Consent Management & Audit Trail Architecture

## Consent Pipeline
1. **Request**: A doctor requests access to a patient profile for a specific care encounter.
2. **Grant**: The patient issues a time-boxed grant specifying granular scopes:
   - `SCOPE_LAB_REPORTS`
   - `SCOPE_PRESCRIPTIONS`
   - `SCOPE_IMAGING`
   - `SCOPE_ADVICE_HISTORY`
   - `TTL`: e.g., 24 hours, 7 days, or until consult completes.
3. **Enforcement**:
   - Backend API verifies policy presence.
   - PostgreSQL Row-Level Security (RLS) dynamically filters rows based on the active session consent token.
4. **Revocation**: Instant revocation (<5 seconds) sets `revoked_at = NOW()`, immediately denying further reads.

## Append-Only Audit & Hash Chain
Every read, consent grant, revocation, and advice record appends an entry to the audit log:

```
Entry N:
{
  "id": "uuid",
  "prev_hash": "hash(Entry N-1)",
  "timestamp": "2026-09-21T22:30:00Z",
  "actor_id": "doctor_uuid",
  "action": "READ_OBSERVATION",
  "resource_id": "obs_uuid",
  "hash": SHA256(prev_hash + canonical_json(entry_body))
}
```

- Tampering with any historical entry invalidates all subsequent hashes.
- Daily Merkle roots are anchored via RFC 3161 Timestamp Authorities (TSA) or OpenTimestamps for external tamper-evidence.
