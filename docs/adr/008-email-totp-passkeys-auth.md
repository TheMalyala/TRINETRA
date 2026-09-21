# ADR 008: Open-Standard Authentication via Email, Argon2id, TOTP, and Passkeys

## Status
Accepted

## Context
Commercial SMS OTP in India requires paid telecom provider contracts and Telecom Regulatory Authority of India (TRAI) Distributed Ledger Technology (DLT) entity registration. This violates Non-Negotiable **NN-10** (no paid core dependencies). Furthermore, SMS is vulnerable to SIM-swapping attacks.

## Decision
We implement authentication using:
1. Email + Password using Argon2id password hashing (`argon2-cffi`).
2. Time-Based One-Time Passwords (TOTP) conforming to RFC 6238 via `pyotp`.
3. FIDO2 / WebAuthn Passkeys.
4. An abstracted `AuthProvider` interface enabling optional ABDM or SMS adapters in the future without changing the core user schema.

## Consequences
- **Positive**: Zero cost, zero commercial lock-in, cryptographically robust against phishing and SIM hijacking.
- **Negative**: Higher UX friction compared to instant mobile SMS in India; mitigated by biometrics and passkey onboarding.
