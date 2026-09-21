# Incident Response & Data Breach Playbook

Compliant with DPDP Act 2023 and DPDP Rules 2025:

1. **Identification**:
   - Automated triggers: Gitleaks CI failure, anomalous bulk download alerts, RLS violation spikes.
2. **Containment**:
   - Immediate session invalidation via Redis token revocation.
   - Isolation of compromised API keys or infrastructure nodes.
3. **Assessment**:
   - Audit trail inspection using the immutable hash chain to determine affected profile IDs.
4. **Intimation to Data Protection Board of India (DPBI) & Users**:
   - Notification to DPBI and affected data principals within statutory timelines detailing breach nature, affected data types, and remedial steps.
5. **Remediation & Post-Mortem**:
   - Security patch deployment, key rotation, root-cause analysis documented in `docs/security/incidents/`.
