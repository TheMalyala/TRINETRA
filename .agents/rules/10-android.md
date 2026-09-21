# Android Architecture Rules

1. **Architecture**: MVVM + Clean Architecture with Unidirectional Data Flow (UDF).
2. **Multi-Module**: Modules must depend only on what they strictly need. Core modules must not depend on feature modules.
3. **Jetpack Compose**:
   - No business logic inside composables.
   - All state must be hoisted via ViewModels exposing StateFlow.
   - Explicit Compose stability annotations (`@Immutable`, `@Stable`) for external models where necessary.
4. **Database & Storage**:
   - Room + SQLCipher for all local databases.
   - Master key stored securely in Android Keystore.
   - `android:allowBackup="false"` in `AndroidManifest.xml`.
5. **No Hard-coded Colors/Strings**:
   - Always reference design tokens from `core:designsystem`.
   - All user-facing strings must live in `res/values/strings.xml` with Indian language support readiness (`res/values-hi/`).
6. **Screen Protection**:
   - Apply `WindowManager.LayoutParams.FLAG_SECURE` to records, AI chat, and consent screens to prevent unauthorized screenshots and display in recents.
