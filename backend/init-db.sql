-- This script runs when the PostgreSQL container starts for the first time
-- It creates the timetracker user and grants necessary permissions

-- Create the timetracker user
CREATE USER timetracker WITH PASSWORD 'password';

-- Grant permissions to the timetracker user for the timetracker database
GRANT ALL PRIVILEGES ON DATABASE timetracker TO timetracker;

-- Connect to the timetracker database to set up schema permissions
\c timetracker

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO timetracker;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO timetracker;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO timetracker;

-- Ensure future tables and sequences are also granted to timetracker
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO timetracker;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO timetracker;