import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Card, Form, InputNumber, Select, Button, Space, Input, Alert } from 'antd';
import { generateExerciseSet } from '../api/exercises.js';

export default function ExerciseGenerator() {
  const nav = useNavigate();
  const { state } = useLocation();
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  const onFinish = async (values) => {
    setErr('');
    setLoading(true);
    try {
      // 兩種輸入都支援：
      // 1) types="mcq,tf" + count=2        -> countMap = { mcq:2, tf:2 }
      // 2) types="mcq:2, tf:2"（各自題數） -> countMap = { mcq:2, tf:2 }
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
        articleId: Number(values.articleId),
        difficulty: values.difficulty,   // 你現在用 "B1" 也 OK，照後端接受的值送
        types: raw,                      // 後端需要 types 就保留；若不需要亦不影響
        count: countMap                  // ✅ 關鍵：count 是 Map<String,Integer>
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
    <Card title="2) 以文章生成題組">
      <Form
        layout="vertical"
        onFinish={onFinish}
        initialValues={{ articleId: state?.articleId || '', difficulty: 'easy', types: 'mcq', count: 5 }}
        style={{ maxWidth: 520 }}
      >
        <Form.Item label="文章 ID" name="articleId" rules={[{ required: true, message: '請輸入文章 ID' }]}>
          <Input type="number" min={1} placeholder="例如：1" />
        </Form.Item>

        <Form.Item label="難度" name="difficulty">
          <Select
            options={[
              { label: 'easy', value: 'easy' },
              { label: 'medium', value: 'medium' },
              { label: 'hard', value: 'hard' },
              // 你也可以手動輸入 B1/B2 等，前端不限制，後端能接受即可
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
      

        {/* 當 types 沒有帶 :n 時，這個共用題數才會生效 */}
        <Form.Item label="題數" name="count">
          <InputNumber min={1} max={50} style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={loading}>產生題組</Button>
            {err && <Alert type="error" showIcon message={err} />}
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
}
