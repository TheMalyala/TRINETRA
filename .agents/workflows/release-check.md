# Workflow: Release Checklist

1. **R8/ProGuard**: Obfuscation and shrinking enabled, debuggable disabled.
2. **Screen Security**: Verify `FLAG_SECURE` is applied on records, chat, and vault screens.
3. **Backup Check**: Confirm auto-backup excludes encrypted DB and keys.
4. **Legal & Compliance**: Verify DPDP notice, consent withdrawal flow, and NMC Telemedicine disclaimer texts.
5. **Vulnerability Scans**: MobSF static analysis clean of high/critical security findings.
