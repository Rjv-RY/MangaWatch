
DROP TABLE IF EXISTS user_library CASCADE;

CREATE TABLE library (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE library_entry (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES library(id) ON DELETE CASCADE,
    manga_id BIGINT NOT NULL REFERENCES manga(id) ON DELETE CASCADE,
    reading_status VARCHAR(32) NOT NULL DEFAULT 'Plan to Read',
    rating INTEGER,
    review TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (library_id, manga_id)
);