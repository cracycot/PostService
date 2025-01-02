CREATE TABLE posts (
    id BIGINT PRIMARY KEY,
    owner_id BIGINT,
    title TEXT,
    content TEXT,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE images
(
    id      BIGINT PRIMARY KEY,
    s3_url  VARCHAR(2083) NOT NULL,
    post_Id BIGINT        NOT NULL REFERENCES posts (id) ON DELETE CASCADE
)