// src/components/ListeningSolve.jsx
import React, { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Card, List, Typography, Skeleton, Space, Button, Tag,
  Form, Radio, Input, Alert
} from 'antd';
import { CustomerServiceOutlined } from '@ant-design/icons';
import { getListeningExercise } from '../api/listening_exercises';
import { submitListeningAnswers } from '../api/listening_submissions';
const { Text, Paragraph } = Typography;

export default function ListeningSolve() {
  const { id } = useParams();

  // 題組載入狀態
  const [ex, setEx] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  // 作答狀態
  const [answers, setAnswers] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitErr, setSubmitErr] = useState('');
  const [result, setResult] = useState(null); // 後端評分回傳

  // ⭐ 新增：Transcript 收合控制
  const [showTranscript, setShowTranscript] = useState(false);

  // 取得題組（含音檔與 transcript）
  useEffect(() => {
    (async () => {
      try {
        setErr('');
        setLoading(true);
        setResult(null);
        setSubmitErr('');

        const data = await getListeningExercise(id);
        setEx(data);
      } catch (e) {
        setErr(e?.response?.data?.message || e.message || '讀取失敗');
      } finally {
        setLoading(false);
      }
    })();
  }, [id]);

  // 依題型初始化答案：MCQ 用索引 number；（保留彈性：若未來有簡答題，以字串處理）
  const initAnswers = useMemo(() => {
    if (!ex || !Array.isArray(ex.items)) return {};
    const ans = {};
    ex.items.forEach((q, idx) => {
      ans[idx] = Array.isArray(q?.options) ? null : '';
    });
    return ans;
  }, [ex]);

  useEffect(() => setAnswers(initAnswers), [initAnswers]);

  // 提交（Listening API 預設採 B 案：answers: number[]）
  const onSubmit = async () => {
    if (!ex) return;
    setSubmitErr('');
    setSubmitting(true);
    try {
      // 把物件 {0:2,1:0,...} 轉為陣列（MCQ 索引）
      const arr = (ex.items || []).map((q, idx) => {
        const a = answers[idx];
        return Array.isArray(q?.options)
          ? (a === null || a === undefined ? -1 : Number(a))
          : String(a ?? '');
      });

    const data = await submitListeningAnswers(Number(ex.id ?? id), arr);
    setResult(data);                  // ⭐ 關鍵：顯示提交後的分數/詳解
    setSubmitErr('');                 // 可選：清掉錯誤訊息
    } catch (e) {
      setSubmitErr(e?.response?.data?.message || e.message || '提交失敗');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Skeleton active />;
  if (err) return <Card><Text type="danger">{err}</Text></Card>;
  if (!ex) return <Card>找不到題組</Card>;

  const items = ex.items || [];
  const difficulty = ex.difficulty ?? ex?.spec?.difficulty ?? '—';
  const topics = ex?.spec?.topics ?? []; // 後端是存陣列
  const genre = ex?.spec?.genre ?? '—';
    const genreMap = {
  short: "短文",
  dialogue: "對話",
};

  const difficultyColorMap = {
  easy: "green",
  medium: "yellow",
  hard: "red",
  };
  const difficultyMap = {
    easy: "簡單",
    medium: "中等",
    hard: "困難",
  };  

  // Transcript 斷行：以空白行優先；其次以單行換行
  const paragraphs = String(ex?.transcript || '')
    .split(/\n\s*\n/) // 空白行分段
    .flatMap(p => p.split(/\n(?!$)/)) // 兼容單行換行
    .map(s => s.trim())
    .filter(Boolean);

  const renderResultSummary = () => {
    if (!result) return null;
    return (
      <Alert
        type="success"
        showIcon
        message={
          <Space wrap>
            <b>
            正確題數: {result.correct} </b>
            </Space>
            }
        style={{ marginTop: 12 }}
      />
    );
  };

  const renderPerQuestion = () => {
    if (!result?.results) return null;
    const rows = result.results;
    return (
      <Card title="作答詳解" style={{ marginTop: 16 }}>
        <List
          itemLayout="vertical"
          dataSource={rows}
          renderItem={(r) => {
            const isMCQ = Array.isArray(r.options);
            return (
              <List.Item key={r.index}>
                <List.Item.Meta
                  title={
                    <Space>
                      <b>Q{r.index + 1}. {r.prompt}</b>
                      {r.correct
                        ? <Tag color="green">✓ 正確</Tag>
                        : <Tag color="red">✗ 錯誤</Tag>}
                    </Space>
                  }
                />
                {isMCQ && (
                  <div style={{ marginBottom: 8 }}>
                    <div style={{ fontSize: 13, opacity: .8, marginBottom: 4 }}>選項：</div>
                    <ul style={{ paddingLeft: 22 }}>
                      {r.options.map((opt, i) => (
                        <li key={i}>
                          {opt}
                          {i === r.correctAnswer && <Tag color="green" style={{ marginLeft: 8 }}>正解</Tag>}
                          {i === r.userAnswer && !r.correct && <Tag color="red" style={{ marginLeft: 8 }}>你的選擇</Tag>}
                          {i === r.userAnswer && r.correct && <Tag style={{ marginLeft: 8 }}>你的選擇</Tag>}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
                {!isMCQ && (
                  <Space wrap>
                    <Tag>你的答案：{String(r.userAnswer ?? '')}</Tag>
                    <Tag color="green">正解：{String(r.correctAnswer ?? '')}</Tag>
                  </Space>
                )}
                {r.explanation && (
                  <div style={{ marginTop: 8, background: '#fafafa', border: '1px solid #eee', borderRadius: 8, padding: 10 }}>
                    <b>解析：</b>
                    <div style={{ whiteSpace: 'pre-wrap' }}>{r.explanation}</div>
                  </div>
                )}
              </List.Item>
            );
          }}
        />
      </Card>
    );
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {/* 音檔 / Transcript 區塊（對齊閱讀版上方資訊卡的角色） */}
      <Card
        title={
          <Space>
            <CustomerServiceOutlined />
            英聽題目
              <Tag color={ difficultyColorMap[difficulty] || "default" }>
                      {difficultyMap[difficulty] || difficulty}
                </Tag>
              <Tag color="blue">{genreMap[genre] || genre}</Tag>

              {Array.isArray(topics) && topics.length > 0 ? (
                topics.map((t, i) => (
                  <Tag key={i} color="purple">{t}</Tag>
                ))
              ) : (
                <Tag>—</Tag>
              )}
          </Space>
        }
        extra={
          <Space>
            {/* ⭐ 新增：收合／展開 transcript 的按鈕 */}
            <Button
            type={showTranscript ? "default" : "primary"}
            danger={!showTranscript}     // 收合狀態時紅色更醒目
            onClick={() => setShowTranscript(v => !v)}
            >
            {showTranscript ? '收合 transcript' : '展開 transcript'}
            </Button>

          </Space>
        }
      >
        {/* 音檔播放器 */}
        {ex.audioUrl ? (
          <audio controls style={{ width: '100%', marginBottom: showTranscript ? 16 : 0 }}>
            <source src={ex.audioUrl} />
            Your browser does not support the audio element.
          </audio>
        ) : (
          <Paragraph type="secondary">(此題組未提供音檔)</Paragraph>
        )}

        {/* Transcript（可卷軸） — 受 showTranscript 控制 */}
        {showTranscript && (
          paragraphs.length > 0 ? (
            <div style={{ maxHeight: 320, overflow: 'auto', paddingRight: 8 }}>
              {paragraphs.map((p, i) => (
                <Paragraph key={i} style={{ lineHeight: 1.7, marginBottom: 12 }}>
                  {p}
                </Paragraph>
              ))}
            </div>
          ) : (
            <Paragraph type="secondary">(無 Transcript)</Paragraph>
          )
        )}
      </Card>

      {/* 題組 + 直接作答（樣式、行為比照 ExerciseViewer） */}
      <Card
        title={
          <Space>
            題組作答
            <Tag color="blue">共 {items.length} 題</Tag>
          </Space>
        }
       
      >
        <Form layout="vertical" onFinish={onSubmit}>
          {items.map((q, idx) => (
            <Form.Item key={idx} label={<b>Q{idx + 1}. {q.question}</b>} required>
              {Array.isArray(q.options) && q.options.length > 0 ? (
                <Radio.Group
                  onChange={e => setAnswers(prev => ({ ...prev, [idx]: Number(e.target.value) }))}
                  value={answers[idx]}
                  style={{ display: 'grid', gap: 6 }}
                >
                  {q.options.map((opt, i) => (
                    <Radio key={i} value={i}>{opt}</Radio>
                  ))}
                </Radio.Group>
              ) : (
                <Input
                  placeholder="你的答案…"
                  value={answers[idx] ?? ''}
                  onChange={e => setAnswers(prev => ({ ...prev, [idx]: e.target.value }))}
                />
              )}
            </Form.Item>
          ))}

          <Space align="center" style={{ marginTop: 8 }}>
            <Button type="primary" htmlType="submit" loading={submitting}>提交作答</Button>
            {submitErr && <Alert type="error" showIcon message={submitErr} />}
          </Space>
        </Form>

        {/* 成功摘要 + 逐題詳解 */}
        <div style={{ marginTop: 16 }}>
          {renderResultSummary()}
          {renderPerQuestion()}
        </div>
      </Card>
    </Space>
  );
}
