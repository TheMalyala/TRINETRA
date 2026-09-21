# TRINETRA — Glossary of Terms

## Clinical & Medical
- **ABDM (Ayushman Bharat Digital Mission)**: India's digital health infrastructure standard enabling interoperable health records.
- **ABHA (Ayushman Bharat Health Account)**: 14-digit unique health identifier issued by the Government of India.
- **LOINC (Logical Observation Identifiers Names and Codes)**: Universal clinical standard for identifying laboratory observations.
- **UCUM (Unified Code for Units of Measure)**: Standard coding system for physical and clinical units of measurement.
- **ICD-11 (International Classification of Diseases 11th Revision)**: WHO standard classification for diagnostic conditions.

## Architectural & Security
- **Local-First**: Architectural paradigm where client database (Room + SQLCipher) is the primary source of truth for patient data.
- **Hash Chaining**: Cryptographic technique where each log entry contains the SHA-256 hash of the preceding entry, forming a tamper-evident audit ledger.
- **RLS (Row-Level Security)**: PostgreSQL access control mechanism ensuring database queries are restricted at the engine level based on session variables.
- **Argon2id**: Memory-hard password hashing algorithm resistant to GPU and side-channel cracking.
- **UDF (Unidirectional Data Flow)**: UI pattern where state flows down and events flow up through explicit ViewModel streams.
- **DEK / KEK**: Data Encryption Key (encrypts data blobs) and Key Encryption Key (wraps DEK in hardware keystore or KMS).
