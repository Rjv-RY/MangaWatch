CREATE TABLE IF NOT EXISTS user_library (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  manga_id BIGINT NOT NULL, -- matches your manga.id
  reading_status VARCHAR(32) NOT NULL DEFAULT 'Plan to Read',
  rating INT NULL,
  review TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),
  UNIQUE (user_id, manga_id)
);