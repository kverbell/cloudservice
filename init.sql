DO $$
    BEGIN
        -- Если база данных не существует, создаем её
        IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_database WHERE datname = 'cloudservice_db') THEN
            PERFORM dblink_exec('host=localhost user=postgres dbname=postgres password=postgres', 'CREATE DATABASE cloudservice_db');
        END IF;
    END $$;

CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(255) UNIQUE NOT NULL,
                                     password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS file_data (
                                         id SERIAL PRIMARY KEY,
                                         user_id INT NOT NULL,
                                         file_name VARCHAR(255) NOT NULL,
                                         file_path VARCHAR(255) NOT NULL,
                                         file_size BIGINT,
                                         upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users (username, password) VALUES ('user1', 'password1')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password) VALUES ('user2', 'password2')
ON CONFLICT (username) DO NOTHING;