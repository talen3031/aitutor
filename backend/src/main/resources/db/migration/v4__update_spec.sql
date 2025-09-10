UPDATE exercise_set_listening
SET spec = jsonb_set(
    spec - 'topic',                          -- 先移除舊的 "topic"
    '{topics}',                              -- 插入 key "topics"
    to_jsonb(ARRAY[spec->>'topic']),         -- 把原本的 topic 轉成 ["topic"]
    true
)
WHERE spec ? 'topic';                        -- 只處理有 "topic" 的 row