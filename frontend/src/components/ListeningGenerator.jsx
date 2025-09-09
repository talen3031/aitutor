import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, InputNumber, Select, Button, Space, Input, Alert } from 'antd';
import { generateListeningExercise } from '../api/listening_exercises'; 
export default function ListeningGenerator() {
  const nav = useNavigate();
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  const onFinish = async (values) => {
    setErr('');
    setLoading(true);
    try {
      const payload = {
        difficulty: values.difficulty,
        numQuestions: Number(values.numQuestions || 0),
        topic: values.topic,
        genre: values.genre,
      };
      const data = await generateListeningExercise(payload);
      const newId = data.id ?? data.exerciseSetId;
      if (!newId) {
        setErr('後端沒有回傳題組 ID');
        return;
      }
      nav(`/listening/${newId}`);
    } catch (e) {
      setErr(e?.response?.data?.message || e.message || '產生失敗');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card title="產生英聽題組">
      <Form
        layout="vertical"
        onFinish={onFinish}
        // 比照 ExerciseGenerator 的預設值行為（此頁不需要 articleId）
        initialValues={{ difficulty: 'medium', numQuestions: 3, topic: '', genre: 'short' }}
        style={{ maxWidth: 520 }}
      >
        <Form.Item label="難度" name="difficulty">
          <Select
            options={[
              { label: 'easy', value: 'easy' },
              { label: 'medium', value: 'medium' },
              { label: 'hard', value: 'hard' },
            ]}
          />
        </Form.Item>

        <Form.Item
          label="題數"
          name="numQuestions"
          rules={[{ required: true, message: '請輸入題數' }]}
        >
          <InputNumber min={1} max={10} style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
            label="主題（topic）"
            name="topic"
            rules={[{ required: true, message: '請輸入主題（例如 finance, travel）' }]}
            getValueFromEvent={(e) => e.target.value.replace(/\s+/g, '')}  // ⭐ 自動移除所有空格
            >
            <Input placeholder="e.g., finance, travel, campus" />
        </Form.Item>

        <Form.Item label="體裁（genre）" name="genre">
          <Select
            options={[
              { label: '對話', value: 'dialogue' },
              { label: '短文', value: 'short' },
            ]}
          />
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
  );
}
