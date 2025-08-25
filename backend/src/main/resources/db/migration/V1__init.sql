CREATE TABLE article (
  id BIGSERIAL PRIMARY KEY,
  title TEXT,
  source TEXT,
  source_url TEXT,
  license TEXT,
  lang TEXT,
  cleaned_text TEXT NOT NULL,
  content_hash TEXT UNIQUE NOT NULL,
  fetched_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE exercise_set (
  id BIGSERIAL PRIMARY KEY,
  article_id BIGINT NOT NULL REFERENCES article(id),
  difficulty TEXT NOT NULL,
  spec TEXT NOT NULL,
  items TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE(article_id, difficulty, spec)
);

CREATE TABLE submission (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  exercise_set_id BIGINT NOT NULL REFERENCES exercise_set(id),
  answers TEXT NOT NULL,
  score NUMERIC,
  detail TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
