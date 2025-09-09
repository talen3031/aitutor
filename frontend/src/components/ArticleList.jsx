// src/components/ArticleList.jsx
import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card, List, Typography, Skeleton, Space, Tag, Button, Empty, Alert } from 'antd';
import { listArticles } from '../api/articles.js';

const { Text } = Typography;

export default function ArticleList() {
  const nav = useNavigate();

  const [items, setItems] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage]   = useState(1);
  const [size, setSize]   = useState(12);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        setErr('');
        setLoading(true);
        const data = await listArticles({ page, size }); // 這個已把後端 0-based 轉成前端格式
        if (!alive) return;
        setItems(data.items);
        setTotal(data.total);
      } catch (e) {
        if (!alive) return;
        setErr(e?.response?.data?.message || e.message || '讀取失敗');
      } finally {
        if (!alive) return;
        setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [page, size]);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title="文章列表">
        {err && <Alert type="error" showIcon message={err} style={{ marginBottom: 12 }} />}

        {loading ? (
          <Skeleton active paragraph={{ rows: 6 }} />
        ) : items.length === 0 ? (
          <Empty description="目前沒有文章" />
        ) : (
          <List
            grid={{ gutter: 16, xs: 1, sm: 2, md: 2, lg: 3, xl: 3, xxl: 4 }}
            dataSource={items}
            pagination={{
              current: page,
              pageSize: size,
              total,
              onChange: (p, s) => { setPage(p); setSize(s); },
              showSizeChanger: true,
              showTotal: (t) => `共 ${t} 筆`
            }}
            renderItem={(a) => {
              const id = a.id;
              const title = a.title || `文章 #${id}`;
              const source = a.source || 'N/A';

              return (
                <List.Item key={id}>
                  <Card
                    hoverable
                    headStyle={{ whiteSpace: 'normal' }} // 讓卡片標題可換行
                    title={
                      // 讓 TITLE 完整顯示：改成直向排版＋可換行
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                          <Tag style={{ width: 'auto', alignSelf: 'flex-start' }}>
                                        {source}
                        </Tag>
                        
                        
                      
                      </div>
                    }
                    actions={[
                      // ✅ 點擊能帶著 articleId 前往生題頁
                      <Button
                        key="gen"
                        type="primary"
                        onClick={() => nav('/exercises/generate', 
                          { state: {  articleId: id,
                                        title: a.title,
                                        url: a.url || a.sourceUrl } })}>
                        以此生成題組
                      </Button>
                    ]}>
                  
                     {/* 內容區：顯示 文章內容 */}
                    <Link to={`/articles/${id}`} style={{ textDecoration: 'none' }}>
                          <span style={{ fontWeight: 700, whiteSpace: 'normal', wordBreak: 'break-word' }}>
                            {title}
                          </span>
                    </Link>
                  </Card>
                </List.Item>
              );
            }}
          />
        )}
      </Card>
    </Space>
  );
}
