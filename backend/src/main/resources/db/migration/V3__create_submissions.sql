-- V3__create_submissions.sql
-- Create table: submissions

CREATE TABLE IF NOT EXISTS submissions (
  id BIGSERIAL PRIMARY KEY,
  exercise_set_id BIGINT NOT NULL,

  -- 原始提交（包含 exerciseSetId + responses/answers）
  answers JSONB,

  -- 逐題比對結果清單
  results JSONB,

  total INT NOT NULL,
  correct INT NOT NULL,
  score DOUBLE PRECISION NOT NULL,

  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 索引（可加速查詢）
CREATE INDEX IF NOT EXISTS idx_submissions_exercise_set_id ON submissions (exercise_set_id);
CREATE INDEX IF NOT EXISTS idx_submissions_created_at ON submissions (created_at);
