# ADR 003: FHIR-Shaped Relational Internal Model with FHIR R4 Export

## Status
Accepted

## Context
Interoperability with ABDM and international healthcare ecosystems requires FHIR R4 compatibility. However, storing native FHIR JSON directly in mobile SQLite and Postgres severely hampers indexing, query speed, table joins, and referential integrity.

## Decision
We define an internal relational schema whose domain entities mirror FHIR resources (Patient, Practitioner, Observation, MedicationRequest, DocumentReference, Appointment, Consent, AuditEvent). A dedicated mapping layer handles lossless export to valid FHIR R4 Bundles adhering to ABDM Indian FHIR profiles.

## Consequences
- **Positive**: Blazing fast SQL queries, strict schema constraints, clean UI models, and full FHIR R4 exportability.
- **Negative**: Mapping layer must be maintained between internal entities and external FHIR profiles.
