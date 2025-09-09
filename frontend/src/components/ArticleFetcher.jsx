import React,{ useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Input, Button, Space, Alert } from 'antd';
import { fetchArticleByUrl } from '../api/articles.js';

export default function ArticleFetcher() {
  const nav = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const onFinish = async ({ url }) => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchArticleByUrl(url.trim());
      // 抓到文章後直接跳轉 → 將 articleId, title, url 傳給 ExerciseGenerator
      nav('/exercises/generate', {
        state: { articleId: data.id, title: data.title, url: data.url || data.sourceUrl }
      });
    } catch (e) {
      setError(e?.response?.data?.message || e.message || '抓取失敗');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title="輸入文章 URL 獲取文章">
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item
            label="文章 URL"
            name="url"
            rules={[{ required: true, type: 'url', message: '請輸入有效的 URL' }]}
          >
            <Input placeholder="https://example.com/article" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading}>抓取並生成題組</Button>
          </Form.Item>
        </Form>
        {error && <Alert type="error" showIcon message={error} style={{ marginTop: 12 }} />}
      </Card>
    </Space>
  );
}
