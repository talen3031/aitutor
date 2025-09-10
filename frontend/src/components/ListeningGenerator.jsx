import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, InputNumber, Select, Button, Space, Alert, Input, Tag } from 'antd';
import { generateListeningExercise } from '../api/listening_exercises'; 

export default function ListeningGenerator() {
  const nav = useNavigate();
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  // ✅ topics 本地狀態（自訂輸入 + 標籤顯示）
  const [topics, setTopics] = useState(['商業']);
  const [topicInput, setTopicInput] = useState('');

  const handleAddTopic = () => {
    if (topicInput && !topics.includes(topicInput)) {
      setTopics([...topics, topicInput]);
    }
    setTopicInput('');
  };

  const handleRemoveTopic = (removed) => {
    setTopics(topics.filter((t) => t !== removed));
  };

  const onFinish = async (values) => {
    setErr('');
    setLoading(true);
    try {
      const payload = {
        difficulty: values.difficulty,
        numQuestions: Number(values.numQuestions || 0),
        topics: topics.length > 0 ? topics : ["general"], // ✅ 使用本地 topics 狀態
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
        initialValues={{ difficulty: 'medium', numQuestions: 3, genre: 'short' }}
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
        {/* ✅ 題數*/}
        <Form.Item
          label="題數"
          name="numQuestions"
          rules={[{ required: true, message: '請輸入題數' }]}
        >
          <InputNumber min={1} max={10} style={{ width: '100%' }} />
        </Form.Item>
        {/* ✅題型選單*/}
        <Form.Item label="題型" name="genre">
          <Select
            options={[
              { label: '對話', value: 'dialogue' },
              { label: '短文', value: 'short' },
            ]}
          />
        </Form.Item>

        {/* ✅ 自訂輸入框 + Tag 列表 */}
        <Form.Item label="主題(可設置多個主題)" required>
          <Input
            placeholder="輸入主題後按 Enter"
            value={topicInput}
            onChange={(e) => setTopicInput(e.target.value)}
            onPressEnter={(e) => {
              e.preventDefault();
              handleAddTopic();
            }}
          />
          <div style={{ marginTop: 8 }}>
            {topics.map((t) => (
              <Tag
                color='purple'
                key={t}
                closable
                onClose={() => handleRemoveTopic(t)}
              >
                {t}
              </Tag>
            ))}
          </div>
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
