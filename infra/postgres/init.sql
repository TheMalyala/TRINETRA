-- Enable necessary PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";
CREATE EXTENSION IF NOT EXISTS "btree_gist";

-- Initial confirmation
DO $$
BEGIN
    RAISE NOTICE 'TRINETRA database initialized with vector and btree_gist extensions';
END $$;
