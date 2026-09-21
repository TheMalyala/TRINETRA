# Workflow: Security Review

1. **Secret Scanning**: Execute Gitleaks against staged files: `gitleaks detect --source . -v`.
2. **Log Audit**: Verify that newly introduced log statements pass through the PHI scrubber.
3. **Database Security**: Ensure SQLCipher encryption configuration is untouched; verify `allowBackup="false"`.
4. **Consent & RLS**: For backend changes, verify Postgres RLS policies prevent cross-patient data access.
5. **Dependency Audit**: Review any added dependencies for licenses and vulnerabilities.
