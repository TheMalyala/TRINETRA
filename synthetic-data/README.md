# Synthetic Clinical Data Strategy

In accordance with Non-Negotiable **NN-14**, developer environments, automated tests, and product demonstrations must use **exclusively synthetic clinical data**. Real patient identifiers (Aadhaar, ABHA, phone numbers, real patient names) must never be committed to git or used in development.

## Data Generation Tools
1. **Synthea**: Open-source, synthetic patient generator modeling clinical histories in FHIR R4 format.
2. **Curated Hand-built Fixtures**: Located under `synthetic-data/fixtures/` for unit testing specific edge cases (abnormal biomarkers, unit mismatches, out-of-range lab reports).

## Directory Structure
- `fixtures/lab_reports/`: Synthetic CBC, lipid profiles, liver function tests (LFT), and renal function tests (KFT).
- `fixtures/prescriptions/`: Synthetic doctor prescriptions with brand and generic medicines.
- `fixtures/bills/`: Synthetic pharmacy and clinic invoice receipts for bill scanning tests.
