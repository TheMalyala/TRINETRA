-- TRINETRA Row-Level Security (RLS) Foundations
-- Enforces consent-gated access directly at the database engine

-- Enable RLS on core clinical tables
ALTER TABLE documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE observations ENABLE ROW LEVEL SECURITY;
ALTER TABLE advice_entries ENABLE ROW LEVEL SECURITY;

-- 1. Patient Owner Access Policy
-- Patients always have full access to records belonging to profiles they own
CREATE POLICY patient_owner_documents_policy ON documents
    FOR ALL
    TO authenticated_role
    USING (
        profile_id IN (
            SELECT id FROM profiles WHERE owner_user_id = current_setting('app.current_user_id', true)::uuid
        )
    );

CREATE POLICY patient_owner_observations_policy ON observations
    FOR ALL
    TO authenticated_role
    USING (
        profile_id IN (
            SELECT id FROM profiles WHERE owner_user_id = current_setting('app.current_user_id', true)::uuid
        )
    );

-- 2. Doctor Active-Consent Access Policy
-- Doctors can only read clinical data if there is an active, unexpired, unrevoked consent grant
CREATE POLICY doctor_consented_documents_policy ON documents
    FOR SELECT
    TO authenticated_role
    USING (
        EXISTS (
            SELECT 1 FROM consents c
            JOIN doctors d ON d.id = c.grantee_doctor_id
            WHERE c.grantor_profile_id = documents.profile_id
              AND d.user_id = current_setting('app.current_user_id', true)::uuid
              AND c.starts_at <= NOW()
              AND c.expires_at > NOW()
              AND c.revoked_at IS NULL
              AND documents.type::text = ANY(c.scopes)
        )
    );

-- 3. Append-Only Audit Trail Policy
-- Prevents UPDATE and DELETE operations on audit_log
ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;

CREATE POLICY audit_log_insert_policy ON audit_log
    FOR INSERT
    TO authenticated_role
    WITH CHECK (true);

CREATE POLICY audit_log_select_policy ON audit_log
    FOR SELECT
    TO authenticated_role
    USING (true);

REVOKE UPDATE, DELETE ON audit_log FROM authenticated_role;
REVOKE UPDATE, DELETE ON advice_entries FROM authenticated_role;
