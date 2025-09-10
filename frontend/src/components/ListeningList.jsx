// src/components/ListeningList.jsx
import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Card, List, Typography, Skeleton, Space, Button, Tag, Empty, Alert
} from 'antd';
import { CustomerServiceOutlined, PlusOutlined } from '@ant-design/icons';
import { listListeningExercises } from '../api/listening_exercises';


const { Text } = Typography;

export default function ListeningList() {
  const nav = useNavigate();

  const [items, setItems] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage]   = useState(1);
  const [size, setSize]   = useState(9);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');
    const genreMap = {
  short: "短文",
  dialogue: "對話",
};
  const difficultyMap = {
    easy: "簡單",
    medium: "中等",
    hard: "困難",
  };
      const difficultyColorMap = {
  easy: "green",
  medium: "yellow",
  hard: "red",
};

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        setErr('');
        setLoading(true);
        // 後端目前回傳整個陣列；先在前端做切頁
        const list = await listListeningExercises();
        if (!alive) return;
        const arr = Array.isArray(list) ? list : (list?.content ?? []);
        setItems(arr);
        setTotal(arr.length);
      } catch (e) {
        if (!alive) return;
        setErr(e?.response?.data?.message || e.message || '讀取失敗');
      } finally {
        if (!alive) return;
        setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, []);

  // 目前以前端切頁；若未來 API 支援分頁，改為依 page/size 重新請求即可
  const pagedItems = useMemo(() => {
    const start = (page - 1) * size;
    return items.slice(start, start + size);
  }, [items, page, size]);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card
        title={(
          <Space>
            <CustomerServiceOutlined />
            <b>英聽列表</b>
          </Space>
        )}
       
      >
        {err && <Alert type="error" showIcon message={err} style={{ marginBottom: 9 }} />}

        {loading ? (
          <Skeleton active paragraph={{ rows: 6 }} />
        ) : total === 0 ? (
          <Empty description="目前沒有英聽題組" />
        ) : (
          <List
            grid={{ gutter: 16, xs: 1, sm: 2, md: 2, lg: 3, xl: 3, xxl: 4 }}
            dataSource={pagedItems}
            pagination={{
              current: page,
              pageSize: size,
              total,
              onChange: (p, s) => { setPage(p); setSize(s); },
              showSizeChanger: false,
              showTotal: (t) => `共 ${t} 筆`
            }}
            renderItem={(x) => {
              const id = x.id;
              const difficulty = x.difficulty ?? x?.spec?.difficulty ?? '—';
              const topics = x?.spec?.topics ?? [];

              const genre = x?.spec?.genre ?? '—';

              return (
                <List.Item key={id}>
                  <Card
                    hoverable
                    headStyle={{ whiteSpace: 'normal' }} // 讓卡片標題可換行
                    title={
                      // 跟 ArticleList 一樣：標題在上、Tag 放在下一行
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                          {/* 第一行：難度 + 題型 */}
                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                            <Tag color={ difficultyColorMap[difficulty] || "default" }>
                              {difficultyMap[difficulty] || difficulty}
                            </Tag>
                            <Tag color="blue">{genreMap[genre] || genre}</Tag>
                          </div>

                          {/* 第二行：topics */}
                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                            {Array.isArray(topics) && topics.length > 0 ? (
                              topics.map((t, i) => (
                                <Tag key={i} color="purple">{t}</Tag>
                              ))
                            ) : (
                              <Tag>—</Tag>
                            )}
                          </div>
                        </div>
                      </div>
                    }
                    actions={[
                      <Link key="solve" to={`/listening/${id}`}>
                        <Button type="primary">前往作答</Button>
                      </Link>
                    ]}
                  >
                    {/* 內容區：顯示 transcript 摘要（可省略） */}
                    {x.transcript ? (
                      <Text type="secondary">
                        {x.transcript.length > 140
                          ? `${x.transcript.slice(0, 140)}…`
                          : x.transcript}
                      </Text>
                    ) : (
                      <Text type="secondary">（無 Transcript）</Text>
                    )}
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
