# Digital Personal Data Protection (DPDP) Compliance Checklist

| Statutory Obligation (DPDP Act 2023 / Rules 2025) | Trinetra Implementation | Verification |
|---|---|---|
| **Itemized Notice (Sec 5)** | Clear multilingual notice presented before requesting consent detailing exact categories and purposes. | UI flow check, localized text audit. |
| **Granular Consent (Sec 6)** | Time-boxed, purpose-specific consent scopes (e.g. lab-only, 24-hour validity). | Unit test on consent creation. |
| **Withdrawal of Consent (Sec 6(4))** | Instant revocation button in UI taking effect in <5 seconds. | RLS test asserting zero records returned post-revocation. |
| **Children's Data / Dependants (Sec 9)** | Verifiable parental consent mechanism recorded before onboarding profiles under 18. | Profile creation constraint tests. |
| **Right to Erasure (Sec 12)** | Patient-initiated data deletion purging Room DB and soft-deleting server shared references with audit log. | DB erasure verification test. |
| **Data Residency** | All server infrastructure and databases hosted within the territory of India. | Cloud deployment configuration check. |
