# Workflow: Phase Gate Checklist

Before declaring any phase complete:
- [ ] `./gradlew assembleDebug test` succeeds without errors.
- [ ] `pytest backend/tests` passes all test suites.
- [ ] Linting (`detekt`, `ktlint`, `ruff`) reports 0 errors.
- [ ] Gitleaks scan passes with 0 findings.
- [ ] No real PHI exists in fixtures or codebase.
- [ ] Architecture Decision Records (ADRs) updated if new patterns were added.
- [ ] Walkthrough artifact generated summarizing changes, screenshots, and test outputs.
- [ ] Stop and wait for user's explicit approval (`GO PHASE <N+1>`).
