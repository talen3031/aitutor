CREATE TABLE exercise_set_listening (
  id BIGSERIAL PRIMARY KEY,
  difficulty TEXT NOT NULL,     -- 難度（easy/medium/hard）
  transcript TEXT NOT NULL,     -- 聽力逐字稿
  audio_url TEXT NOT NULL,      -- 聲音檔網址
  spec JSONB NOT NULL,          -- 題目生成規格（題數、題型配置等）
  items JSONB NOT NULL,         -- 題目本身（題目、選項、答案）
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE(difficulty, spec)      -- 保證同一難度 & spec 不會重複
);


CREATE TABLE submissions_listening (
  id BIGSERIAL PRIMARY KEY,
  exercise_set_id BIGINT NOT NULL REFERENCES exercise_set_listening(id),
  answers JSONB,
  results JSONB,
  total   INT NOT NULL,
  correct INT NOT NULL,
  score   DOUBLE PRECISION NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
ALTER TABLE exercise_set RENAME TO exercise_set_reading;
ALTER TABLE submissions RENAME TO submissions_reading;
ALTER TABLE submissions_reading
  RENAME CONSTRAINT fk_submissions_exercise_set
  TO fk_submissions_reading_exercise_set;