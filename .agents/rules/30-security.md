# Security & Privacy Rules

1. **Authentication**:
   - Password hashing via Argon2id (`argon2-cffi`).
   - TOTP 2FA compliant with RFC 6238 via `pyotp`.
   - Access tokens: short-lived JWT (15 minutes). Refresh tokens: rotating, stored securely.
2. **Local Key Management**:
   - Android Keystore master key wraps database and data encryption keys (DEK).
   - Biometric authentication via `BiometricPrompt` gates key access for sensitive actions.
3. **Audit Trail & Integrity**:
   - Advice Ledger and audit records are append-only.
   - SHA-256 hash chaining: each entry links to `prev_hash` and hashes `(prev_hash + canonical_json)`.
   - Doctor-signed advice incorporates an ECDSA P-256 signature.
4. **Data Protection & DPDP**:
   - Purpose limitation, explicit consent scopes, time-boxed validity, and instant revocation (<5 seconds).
   - Verifiable parental consent required for any dependent profile under 18.
   - Never commit secrets or real patient data. Gitleaks checks must pass in CI.
