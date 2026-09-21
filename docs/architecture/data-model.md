# Data Model Specification

The internal data model is relational and FHIR-aligned (ADR-003), enabling efficient local SQL queries while remaining directly exportable to FHIR R4 Bundles.

## Core Entities

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│    users     │1       *│   profiles   │1       *│  encounters  │
│──────────────│─────────│──────────────│─────────│──────────────│
│ id           │         │ id           │         │ id           │
│ email        │         │ owner_user_id│         │ profile_id   │
│ pw_hash      │         │ relationship │         │ doctor_id    │
│ role         │         │ name         │         │ date         │
└──────────────┘         └──────────────┘         └──────────────┘
                                │                        │
                                │1                       │1
                                │*                       │*
                         ┌──────────────┐         ┌──────────────┐
                         │  documents   │         │ observations │
                         │──────────────│         │──────────────│
                         │ id           │         │ id           │
                         │ profile_id   │         │ document_id  │
                         │ encounter_id │         │ loinc_code   │
                         │ sha256       │         │ value_num    │
                         │ ocr_text     │         │ unit_ucum    │
                         └──────────────┘         │ ref_low/high │
                                                  └──────────────┘
```

### 1. `users`
- `id` (UUID, PK)
- `email` (VARCHAR, Unique)
- `pw_hash` (VARCHAR, Argon2id)
- `totp_secret_enc` (BYTEA)
- `role` (ENUM: 'patient', 'doctor', 'admin')
- `status` (ENUM: 'active', 'suspended', 'pending_verification')

### 2. `profiles`
- `id` (UUID, PK)
- `owner_user_id` (UUID, FK -> users.id)
- `relationship` (ENUM: 'self', 'child', 'parent', 'other')
- `name` (VARCHAR)
- `dob` (DATE)
- `sex` (VARCHAR)
- `blood_group` (VARCHAR)
- `guardian_consent_id` (UUID, Nullable)

### 3. `doctors`
- `id` (UUID, PK)
- `user_id` (UUID, FK -> users.id, Nullable for self-reported doctors)
- `display_name` (VARCHAR)
- `registration_no` (VARCHAR)
- `council` (VARCHAR)
- `verification_status` (ENUM: 'unverified', 'pending', 'verified', 'rejected')
- `clinic_name` (VARCHAR)
- `phone` (VARCHAR)
- `email` (VARCHAR)

### 4. `documents`
- `id` (UUID, PK)
- `profile_id` (UUID, FK -> profiles.id)
- `encounter_id` (UUID, FK -> encounters.id, Nullable)
- `doctor_id` (UUID, FK -> doctors.id, Nullable)
- `type` (ENUM: 'lab', 'imaging', 'prescription', 'discharge', 'bill', 'other')
- `sha256` (VARCHAR(64))
- `phash` (VARCHAR(64), Nullable)
- `storage_ref` (VARCHAR)
- `ocr_text` (TEXT)
- `version` (INT, Default 1)
- `supersedes_id` (UUID, Nullable)

### 5. `observations`
- `id` (UUID, PK)
- `profile_id` (UUID, FK -> profiles.id)
- `document_id` (UUID, FK -> documents.id)
- `loinc_code` (VARCHAR(20), Indexed)
- `name_as_printed` (VARCHAR)
- `value_num` (NUMERIC, Nullable)
- `value_text` (VARCHAR, Nullable)
- `unit_ucum` (VARCHAR(30))
- `ref_low` (NUMERIC, Nullable)
- `ref_high` (NUMERIC, Nullable)
- `ref_source` (ENUM: 'report', 'generic')
- `observed_at` (TIMESTAMPTZ)
- `confirmed_by_user_at` (TIMESTAMPTZ)
- `source_bbox` (JSONB, Nullable)

### 6. `advice_entries` (Hash-Chained)
- `id` (UUID, PK)
- `profile_id` (UUID, FK -> profiles.id)
- `doctor_id` (UUID, FK -> doctors.id)
- `body` (TEXT)
- `tags` (VARCHAR[])
- `follow_up_on` (DATE, Nullable)
- `tests_ordered` (VARCHAR[])
- `origin` (ENUM: 'self_reported', 'doctor_signed')
- `signature` (BYTEA, ECDSA P-256 signature, Nullable)
- `prev_hash` (VARCHAR(64))
- `hash` (VARCHAR(64))
- `version` (INT)

### 7. `appointments` (Exclusion-Constrained)
- `id` (UUID, PK)
- `doctor_id` (UUID, FK -> doctors.id)
- `profile_id` (UUID, FK -> profiles.id)
- `slot_range` (TSTZRANGE)
- `status` (ENUM: 'requested', 'confirmed', 'completed', 'cancelled')
- Constraint: `EXCLUDE USING gist (doctor_id WITH =, slot_range WITH &&) WHERE (status IN ('requested', 'confirmed'))`
