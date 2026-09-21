# Screen Catalog & Navigation Flow

## Patient Navigation Shell
Bottom Navigation Bar (5 destinations):
1. **Home**: Today's medications, upcoming appointments, recent observations summary, quick scan FAB.
2. **Vault**: Segmented: Records | Prescriptions | Bills. Filters by doctor, specialty, date, and type.
3. **Ask (Center, Third-Eye)**: Assistant chat (Netra), Jargon Decoder, Report Explainer.
4. **Inbox**: Conversations with Care Circle doctors.
5. **Care**: Doctors directory, Appointments, Advice Ledger, Consent Manager, Emergency Card.

## Doctor Navigation Shell
Bottom Navigation Bar (5 destinations):
1. **Today**: Confirmed appointments, pending messages, follow-ups due.
2. **Patients**: Consented patient workspace, recent vitals, "what changed" timeline.
3. **Schedule**: RRULE recurring weekly availability editor and exceptions/leaves.
4. **Inbox**: Patient messaging threads.
5. **Me**: Registration details, clinic location, verification badge status.

## Screen Inventory (Key Flows)
- `SplashActivity` -> `OnboardingActivity` (3 calm introductory screens)
- `AuthScreen` -> `TOTPEnrollScreen` -> `AppLockSetupScreen`
- `ScannerScreen` -> `OCRProcessingScreen` -> `ConfirmValuesScreen` (crop beside value)
- `BiomarkerDetailScreen` (Vico chart + reference band + medication overlay)
- `AdviceComposeScreen` (ECDSA P-256 signing)
- `EmergencyCardScreen` (Restricted profile, blood group, allergies, QR code)
