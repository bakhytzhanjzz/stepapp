-- Пользователи системы
CREATE TABLE users (
                       id              BIGSERIAL PRIMARY KEY,
                       email           VARCHAR(255) NOT NULL UNIQUE,
                       username        VARCHAR(50) NOT NULL UNIQUE,
                       password_hash   VARCHAR(255) NOT NULL,
                       full_name       VARCHAR(100),
                       avatar_url      VARCHAR(500),
                       timezone        VARCHAR(50) DEFAULT 'UTC',
                       created_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
                       updated_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
                       last_login_at   TIMESTAMP WITH TIME ZONE
);

-- Для контроля изменений (кто создал/обновил)
CREATE TABLE audit_log (
                           id              BIGSERIAL PRIMARY KEY,
                           user_id         BIGINT REFERENCES users(id) ON DELETE SET NULL,
                           action          VARCHAR(100) NOT NULL,
                           payload         JSONB,
                           created_at      TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Индексы для быстрого поиска
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_audit_user_id ON audit_log(user_id);
