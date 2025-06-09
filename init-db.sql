-- Time Tracker Database Initialization Script
-- This script creates the database user and sets up basic configuration

-- Create timetracker user if it doesn't exist (for development)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'timetracker') THEN
        CREATE USER timetracker WITH PASSWORD 'password';
    END IF;
END
$$;

-- Grant necessary permissions to timetracker user
GRANT ALL PRIVILEGES ON DATABASE timetracker TO timetracker;
GRANT ALL PRIVILEGES ON SCHEMA public TO timetracker;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO timetracker;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO timetracker;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO timetracker;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO timetracker;

-- Log initialization completion
\echo 'Database initialization completed successfully'