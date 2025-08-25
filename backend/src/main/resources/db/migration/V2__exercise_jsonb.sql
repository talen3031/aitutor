ALTER TABLE exercise_set
  ALTER COLUMN spec  TYPE jsonb USING spec::jsonb,
  ALTER COLUMN items TYPE jsonb USING items::jsonb;
