-- Initial schema for Time Tracker application
-- Version: 1.0.0
-- Description: Create users, categories, tasks, and time_entries tables

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    google_id VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    time_zone VARCHAR(50) DEFAULT 'UTC',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_archived BOOLEAN DEFAULT FALSE,
    is_default BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, title)
);

-- Tasks table
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    color VARCHAR(7) NOT NULL, -- Hex color code
    icon VARCHAR(50), -- Icon identifier
    is_archived BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(category_id, title),
    UNIQUE(category_id, color) -- Unique color per category
);

-- Time entries table
CREATE TABLE time_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    entry_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_minutes INTEGER NOT NULL,
    description TEXT,
    is_billable BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_time_entries_user_date ON time_entries(user_id, entry_date);
CREATE INDEX idx_time_entries_task ON time_entries(task_id);
CREATE INDEX idx_time_entries_date_time ON time_entries(entry_date, start_time, end_time);
CREATE INDEX idx_categories_user ON categories(user_id);
CREATE INDEX idx_tasks_category ON tasks(category_id);

-- Constraint to prevent overlapping time entries for the same user on the same date
-- Note: This uses a custom function to handle time overlap checking
CREATE OR REPLACE FUNCTION check_time_overlap()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM time_entries
        WHERE user_id = NEW.user_id
        AND entry_date = NEW.entry_date
        AND id != COALESCE(NEW.id, 0)
        AND (
            (NEW.start_time >= start_time AND NEW.start_time < end_time) OR
            (NEW.end_time > start_time AND NEW.end_time <= end_time) OR
            (NEW.start_time <= start_time AND NEW.end_time >= end_time)
        )
    ) THEN
        RAISE EXCEPTION 'Time entry overlaps with existing entry';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for overlap checking
CREATE TRIGGER trigger_check_time_overlap
    BEFORE INSERT OR UPDATE ON time_entries
    FOR EACH ROW
    EXECUTE FUNCTION check_time_overlap();

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for auto-updating updated_at
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_time_entries_updated_at
    BEFORE UPDATE ON time_entries
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert default user for local mode (self-hosted)
INSERT INTO users (email, name, time_zone)
VALUES ('admin@localhost', 'Local Admin', 'UTC');

-- Insert default category for the local user
INSERT INTO categories (user_id, title, description, is_default)
VALUES (1, 'General', 'Default category for time tracking', TRUE);

-- Insert some default tasks
INSERT INTO tasks (category_id, title, color, icon) VALUES
(1, 'Work', '#3498db', 'briefcase'),
(1, 'Break', '#e74c3c', 'coffee'),
(1, 'Meeting', '#9b59b6', 'users'),
(1, 'Learning', '#f39c12', 'book');

-- Comments for rollback
/*
ROLLBACK INSTRUCTIONS:
1. DROP TRIGGER trigger_check_time_overlap ON time_entries;
2. DROP FUNCTION check_time_overlap();
3. DROP TRIGGER update_users_updated_at ON users;
4. DROP TRIGGER update_categories_updated_at ON categories;
5. DROP TRIGGER update_tasks_updated_at ON tasks;
6. DROP TRIGGER update_time_entries_updated_at ON time_entries;
7. DROP FUNCTION update_updated_at_column();
8. DROP TABLE time_entries;
9. DROP TABLE tasks;
10. DROP TABLE categories;
11. DROP TABLE users;
*/