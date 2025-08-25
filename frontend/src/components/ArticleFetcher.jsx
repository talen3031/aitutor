import React,{ useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Card, Form, Input, Button, Space, Alert, Typography, Divider } from 'antd';
import { fetchArticleByUrl } from '../api/articles.js';

const { Paragraph, Text } = Typography;

export default function ArticleFetcher() {
  const nav = useNavigate();
  const [loading, setLoading] = useState(false);
  const [preview, setPreview] = useState(null);
  const [error, setError] = useState('');

  const onFinish = async ({ url }) => {
    setLoading(true);
    setError('');
    setPreview(null);
    try {
      const data = await fetchArticleByUrl(url.trim());
      setPreview(data);
    } catch (e) {
      setError(e?.response?.data?.message || e.message || '抓取失敗');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title="1) 輸入文章 URL → 抓文並清洗">
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item label="文章 URL" name="url" rules={[{ required: true, type: 'url', message: '請輸入有效的 URL' }]}>
            <Input placeholder="https://example.com/article" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading}>抓取</Button>
          </Form.Item>
        </Form>
        {error && <Alert type="error" showIcon message={error} style={{ marginTop: 12 }} />}
      </Card>

      {preview && (
        <Card
          title={<Space split={<Divider type="vertical" />}><Text>{preview.source || '來源'}</Text><a href={preview.url} target="_blank" rel="noreferrer">原文連結</a></Space>}
          extra={
            <Space>
              <Link to={`/articles/${preview.id}`}>
                <Button>查看全文</Button>
              </Link>
              <Button type="primary" onClick={() => nav('/exercises/generate', { state: { articleId: preview.id } })}>
                以此文章生成題組
              </Button>
            </Space>
          }
        >
          <Paragraph ellipsis={{ rows: 8, expandable: true, symbol: '展開全文' }}>
            {preview.text}
          </Paragraph>
        </Card>
      )}
    </Space>
  );
}
