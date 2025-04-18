-- Drop the existing table if it exists
DROP TABLE IF EXISTS user_entity;

-- Create the table with auto-incrementing ID
CREATE TABLE user_entity (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255)
); 