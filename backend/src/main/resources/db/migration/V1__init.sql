-- 初始化資料庫 schema (整合 V1, V2, V3，移除舊 submission)

-- 文章表
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

-- 題組表
CREATE TABLE exercise_set (
  id BIGSERIAL PRIMARY KEY,
  article_id BIGINT NOT NULL REFERENCES article(id),
  difficulty TEXT NOT NULL,
  spec  JSONB NOT NULL,
  items JSONB NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE(article_id, difficulty, spec)
);

-- 使用者提交表 (新版，JSONB 儲存答案與結果)
CREATE TABLE submissions (
  id BIGSERIAL PRIMARY KEY,
  exercise_set_id BIGINT NOT NULL,
  answers JSONB,          -- 使用者提交答案
  results JSONB,          -- 逐題比對結果
  total   INT NOT NULL,   -- 題目總數
  correct INT NOT NULL,   -- 答對數
  score   DOUBLE PRECISION NOT NULL, -- 分數
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_submissions_exercise_set
    FOREIGN KEY (exercise_set_id) REFERENCES exercise_set(id)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_submissions_exercise_set_id ON submissions (exercise_set_id);
CREATE INDEX IF NOT EXISTS idx_submissions_created_at ON submissions (created_at);
