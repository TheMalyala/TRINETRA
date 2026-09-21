# Android Permissions Rationale & Justifications

Trinetra follows the principle of least privilege. Permissions are requested contextually at runtime with clear rationale dialogues.

| Permission | Protection Level | Purpose / Justification | Fallback if Denied |
|---|---|---|---|
| `android.permission.CAMERA` | Dangerous (Runtime) | Document scanner and receipt capture for physical records. | Import PDF or image from system document picker (`ACTION_OPEN_DOCUMENT`). |
| `android.permission.USE_BIOMETRIC` | Normal | Gating local database encryption key via `BiometricPrompt`. | Fallback to custom in-app PIN or password. |
| `android.permission.POST_NOTIFICATIONS` | Dangerous (Runtime) | Timely reminders for medication doses and appointment follow-ups. | In-app notification cards and banners only. |
| `android.permission.SCHEDULE_EXACT_ALARM` | Special / User-granted | Precision scheduling for critical medication doses across OEM sleep states. | Inexact alarms via WorkManager. |
| `android.permission.RECORD_AUDIO` | Dangerous (Runtime) | Hands-free voice assistant "Netra" and voice note attachments. | Text-only assistant and typing. |
| `android.permission.INTERNET` | Normal | Synchronization of shared records, doctor messaging, and secure backups. | Full offline functionality using local SQLCipher DB. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | WorkManager connectivity constraints for sync outbox. | Default periodic background sync attempts. |

**Explicitly Excluded Permissions:**
- No `READ_CONTACTS` or `WRITE_CONTACTS`.
- No `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`.
- No broad `MANAGE_EXTERNAL_STORAGE` or legacy `READ_EXTERNAL_STORAGE`.
