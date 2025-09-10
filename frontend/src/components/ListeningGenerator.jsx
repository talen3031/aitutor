import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, InputNumber, Select, Button, Space, Alert, Input, Tag } from 'antd';
import { generateListeningExercise } from '../api/listening_exercises'; 

export default function ListeningGenerator() {
  const nav = useNavigate();
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');
  
  // ✅ topics 本地狀態（自訂輸入 + 標籤顯示）
  const [topics, setTopics] = useState([]);
  const [topicInput, setTopicInput] = useState('');
  const [topicErr, setTopicErr] = useState('');
  const [numErr, setNumErr] = useState('');

  const handleAddTopic = () => {
    const trimmed = topicInput.trim();
    if (!trimmed) return;

    // 中間包含空格 → 顯示錯誤
    if (/\s/.test(trimmed) && trimmed.includes(' ')) {
      setTopicErr('主題不能包含空格');
      return;
    }

    if (topics.includes(trimmed)) {
      setTopicErr(`已包含主題「${trimmed}」`);
      return;
    }

    setTopics([...topics, trimmed]);
    setTopicInput('');
    setTopicErr('');
  };

  const handleRemoveTopic = (removed) => {
    setTopics(topics.filter((t) => t !== removed));
  };

  const onFinish = async (values) => {
    setErr('');
    setLoading(true);
    
    try {
       // ✅ 題數檢查
        if (values.numQuestions < 1 || values.numQuestions > 5) {
          setNumErr('題數必須介於 1 到 5');
          setLoading(false);
          return;
        } else {
          setNumErr('');
        }
        // ✅ 主題檢查
        if (topics.length === 0) {
        setErr('請至少輸入一個主題');
        setLoading(false);
        return;
      }
      const payload = {
        difficulty: values.difficulty,
        numQuestions: Number(values.numQuestions || 0),
        topics: topics,
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
        initialValues={{ difficulty: 'medium',genre: 'short' }}
        style={{ maxWidth: 520 }}
      >
        <Form.Item label="難度" name="difficulty">
          <Select
            options={[
              { label: '簡單', value: 'easy' },
              { label: '中等', value: 'medium' },
              { label: '困難', value: 'hard' },
            ]}
          />
        </Form.Item>
        {/* ✅ 題數*/}
        <Form.Item label="題數" name="numQuestions">
          <InputNumber min={1} max={5} defaultValue={3} style={{ width: '100%' }} />
          {numErr && (
            <Alert
              type="error"
              showIcon
              message={numErr}
              style={{ marginTop: 8 }}
            />
          )}
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
            onChange={(e) => {
              setTopicInput(e.target.value);
              if (topicErr) setTopicErr(''); // 使用者輸入新字就清除錯誤
            }}            
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
          {topicErr && (
            <Alert
              type="error"
              showIcon
              message={topicErr}
              style={{ marginTop: 8 }}
            />
          )}
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
