# Data Retention & Erasure Policy

1. **Patient Data Erasure**:
   - Patients maintain full statutory rights under DPDP to erase their personal health records.
   - Erasure on device purges SQLCipher SQLite database tables immediately.
   - Erasure on backend anonymizes or soft-deletes records while preserving tamper-evident hash integrity in the audit ledger with an explicit "DELETED_BY_PRINCIPAL" audit event.
2. **Clinical Audit Retention**:
   - In accordance with medical ethics guidelines, signed advice and consultation audit trails are retained for a minimum of 3 years to support legal accountability.
3. **Transient Logs**:
   - Server application logs are scrubbed of all PHI and rotated out after 30 days.
