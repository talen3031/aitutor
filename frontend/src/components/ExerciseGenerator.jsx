import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Card, Form, InputNumber, Select, Button, Space, Input, Alert } from 'antd';
import { generateExerciseSet } from '../api/reading_exercises.js';

export default function ExerciseGenerator() {
  const nav = useNavigate();
  const { state } = useLocation(); // 從 ArticleFetcher 帶進來
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  const onFinish = async (values) => {
    setErr('');
    setLoading(true);
    try {
      const raw = String(values.types || '').split(',').map(s => s.trim()).filter(Boolean);
      const perTypeSyntax = raw.some(s => s.includes(':'));

      let countMap = {};
      if (perTypeSyntax) {
        raw.forEach(pair => {
          const [t, n] = pair.split(':').map(x => x.trim());
          if (t) countMap[t] = Number(n || 0);
        });
      } else {
        const n = Number(values.count || 0);
        raw.forEach(t => { countMap[t] = n; });
      }

      const payload = {
        articleId: Number(state.articleId), // 還是要傳給後端
        difficulty: values.difficulty,
        types: raw,
        count: countMap
      };

      const data = await generateExerciseSet(payload);
      const newId = data.exerciseSetId ?? data.id;
      if (!newId) {
        setErr('後端沒有回傳題組 ID');
        return;
      }
      nav(`/exercises/${newId}`);
    } catch (e) {
      setErr(e?.response?.data?.message || e.message || '產生失敗');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ width: '30vw', padding: 24 }}>
      <div style={{ maxWidth: 1200, margin: '0 auto' }}>
        <Card
          title="以文章生成題組"
          style={{ width: '100%' }}
        >
          <Form
            layout="vertical"
            onFinish={onFinish}
            initialValues={{
              difficulty: 'easy',
              types: 'mcq',
              count: 5
            }}
            style={{ width: '100%' }}
          >
            {/* 顯示文章資訊（唯讀） */}
            <Form.Item label="文章標題">
              <Input.TextArea
                value={state?.title || '（無標題）'}
                readOnly
                autoSize={{ minRows: 1, maxRows: 3 }}
                style={{ whiteSpace: 'normal', wordBreak: 'break-word' }}
              />
            </Form.Item>

            <Form.Item label="原文連結">
              <Input.TextArea
                value={state?.url || ''}
                readOnly
                autoSize={{ minRows: 1, maxRows: 6 }}
                style={{ wordBreak: 'break-all' }}
              />
            </Form.Item>

            <Form.Item label="難度" name="difficulty">
              <Select
                options={[
                  { label: '簡單', value: 'easy' },
                  { label: '中等', value: 'medium' },
                  { label: '困難', value: 'hard' }
                ]}
              />
            </Form.Item>

            <Form.Item label="題型" name="types">
              <Select
                options={[
                  { label: '選擇題', value: 'mcq' },
                  { label: '是非題', value: 'tf' },
                ]}
              />
            </Form.Item>

            <Form.Item label="題數" name="count">
              <InputNumber min={1} max={5} style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={loading}>
                  產生題組
                </Button>
                {err && <Alert type="error" showIcon message={err} />}
              </Space>
            </Form.Item>
          </Form>
        </Card>
      </div>
    </div>
  );
}
