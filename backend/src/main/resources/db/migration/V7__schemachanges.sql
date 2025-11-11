
ALTER TABLE manga 
	ALTER COLUMN rating DROP NOT NULL;

CREATE TABLE IF NOT EXISTS manga_alt_titles (
    manga_id BIGINT NOT NULL,
    alt_title VARCHAR(500) NOT NULL,
    CONSTRAINT fk_manga_alt FOREIGN KEY (manga_id)
        REFERENCES manga (id) ON DELETE CASCADE
);