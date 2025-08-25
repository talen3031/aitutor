import React,{ useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Card, Descriptions, Typography, Skeleton } from 'antd';
import { getArticle } from '../api/articles.js';

const { Paragraph, Text } = Typography;

export default function ArticleViewer() {
  const { id } = useParams();
  const [article, setArticle] = useState(null);
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        setErr('');
        setLoading(true);
        const data = await getArticle(id);
        setArticle(data);
      } catch (e) {
        setErr(e?.response?.data?.message || e.message || '讀取失敗');
      } finally {
        setLoading(false);
      }
    })();
  }, [id]);

  if (loading) return <Skeleton active />;
  if (err) return <Card><Text type="danger">{err}</Text></Card>;
  if (!article) return <Card>找不到文章</Card>;

  // 來源連結欄位兼容 url / sourceUrl
  const sourceLink = article.url || article.sourceUrl || '';

  // 內容：優先使用 paragraphs（字串陣列）；否則用 cleanedText 以 \n\n 切段
  const paragraphs = Array.isArray(article.paragraphs) && article.paragraphs.length > 0
    ? article.paragraphs
    : String(article.cleanedText || article.text || '')
        .split(/\n\s*\n/) // 更寬鬆地切空白段落
        .filter(p => p.trim().length > 0);

  return (
    <Card title={article.title ? `文章標題｜${article.title}` : '文章全文'}>
      <Descriptions column={1} size="small" bordered style={{ marginBottom: 12 }}>
        <Descriptions.Item label="來源">{article.source || 'N/A'}</Descriptions.Item>
        <Descriptions.Item label="原文連結">
          {sourceLink
            ? <a href={sourceLink} target="_blank" rel="noreferrer">{sourceLink}</a>
            : '—'}
        </Descriptions.Item>
    
      </Descriptions>

      {paragraphs.length > 0 ? (
        paragraphs.map((p, i) => (
          <Paragraph key={i} style={{ lineHeight: 1.7, marginBottom: 16 }}>
            {p}
          </Paragraph>
        ))
      ) : (
        <Paragraph>(無內容)</Paragraph>
      )}
    </Card>
  );
}
