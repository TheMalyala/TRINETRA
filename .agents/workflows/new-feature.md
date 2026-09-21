# Workflow: New Feature Implementation

1. **Verify Contract**: Ensure changes to API contracts are documented in `docs/api/openapi.yaml`.
2. **Domain First**: Define or update models in `core:model` (Android) and `app/db/models` (Backend).
3. **Write Failing Tests**:
   - Pure domain logic tests using JUnit5 / MockK / Turbine (Android).
   - Route and business logic tests using pytest (Backend).
4. **Implement**:
   - Write clean, idiomatic code adhering to module boundaries.
   - For UI, use only tokens from `core:designsystem`.
5. **Lint & Security Check**:
   - Run `detekt`, `ktlint`, and `ruff`.
   - Ensure no hard-coded colors or strings are introduced.
6. **Documentation**: Update relevant `docs/` files and ADRs if an architectural pattern was altered.
