# Key Management & Cryptographic Hierarchy

```
[ Android Keystore (Hardware-backed Master Key) ]
                    │
                    ▼ wraps
[ 256-bit Random SQLCipher Database Key ] ──► Encrypts local Room database
                    │
[ User Passphrase + Salt ] ──(Argon2id KDF)──► [ Backup Key (AES-256-GCM) ] ──► Client Encrypted Backup

--------------------------------------------------------------------------------

[ Docker Secrets / KMS Master Key (KEK) ]
                    │
                    ▼ wraps
[ Tenant Data Encryption Key (DEK) ] ──► Encrypts S3 storage blobs & DB message envelopes
```

## Rotation Procedures
1. **Device DB Key**: Key regenerated upon database re-encryption; Keystore entry replaced atomically.
2. **Client Backup Key**: Derived directly on device from user-chosen passphrase; never transmitted to backend.
3. **Server KEK / DEK**: Managed via OpenBao / KMS with automated envelope re-wrapping runbooks.
