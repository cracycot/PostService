DROP TABLE IF EXISTS images;
DROP TABLE IF EXISTS posts;
CREATE TABLE posts (
                       id BIGSERIAL PRIMARY KEY,
                       owner_id BIGINT,
                       title TEXT,
                       content TEXT,
                       create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE images
(
    id  BIGSERIAL PRIMARY KEY,
    s3_url  VARCHAR(2083) NOT NULL,
    post_Id BIGINT        NOT NULL REFERENCES posts (id) ON DELETE CASCADE
)