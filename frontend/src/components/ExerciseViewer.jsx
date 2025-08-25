// src/components/ExerciseViewer.jsx
import React,{ useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Card, List, Typography, Skeleton, Space, Button, Tag, Descriptions,
  Form, Radio, Input, Alert
} from 'antd';
import { getExerciseSet } from '../api/exercises.js';
import { getArticle } from '../api/articles.js';
import { submitAnswers } from '../api/submissions.js';

const { Text, Paragraph } = Typography;

export default function ExerciseViewer() {
  const { id } = useParams();

  // 題組 & 文章載入狀態
  const [ex, setEx] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  const [article, setArticle] = useState(null);
  const [articleErr, setArticleErr] = useState('');
  const [articleLoading, setArticleLoading] = useState(false);

  // 作答狀態
  const [answers, setAnswers] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitErr, setSubmitErr] = useState('');
  const [result, setResult] = useState(null); // 後端評分回傳

  // 載入題組 + 文章
  useEffect(() => {
    (async () => {
      try {
        setErr('');
        setLoading(true);
        setArticle(null);
        setArticleErr('');
        setResult(null);
        setSubmitErr('');

        const data = await getExerciseSet(id);
        setEx(data);

        const aid = data?.articleId ?? data?.spec?.articleId;
        if (aid) {
          setArticleLoading(true);
          try {
            const a = await getArticle(aid);
            setArticle(a);
          } catch (e) {
            setArticleErr(e?.response?.data?.message || e.message || '文章讀取失敗');
          } finally {
            setArticleLoading(false);
          }
        }
      } catch (e) {
        setErr(e?.response?.data?.message || e.message || '讀取失敗');
      } finally {
        setLoading(false);
      }
    })();
  }, [id]);

  // 依題型初始化答案（MCQ 用選項索引 number；其他題型用字串）
  const initAnswers = useMemo(() => {
    if (!ex || !Array.isArray(ex.items)) return {};
    const ans = {};
    ex.items.forEach((q, idx) => {
      ans[idx] = Array.isArray(q?.options) ? null : '';
    });
    return ans;
  }, [ex]);

  useEffect(() => setAnswers(initAnswers), [initAnswers]);

  // 送出作答（使用你先前設計的 payload：MCQ 傳索引）
  const onSubmit = async () => {
    if (!ex) return;
    setSubmitErr('');
    setSubmitting(true);
    try {
      const responses = (ex.items || []).map((q, idx) => {
        const a = answers[idx];
        if (Array.isArray(q?.options)) {
          // 未作答給 -1（若後端不接受可改成 null 或其他預設）
          return { index: idx, answer: (a === null || a === undefined) ? -1 : Number(a) };
        }
        return { index: idx, answer: a ?? '' };
      });

      const payload = { exerciseSetId: Number(ex.id), responses };
      const data = await submitAnswers(payload); // { score,total,submissionId,correct,exerciseSetId,results:[...] }
      setResult(data);
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
  const articleId = ex.articleId ?? ex?.spec?.articleId ?? '—';

  // 文章欄位兼容處理
  const sourceLink = article?.url || article?.sourceUrl || '';
  const paragraphs = Array.isArray(article?.paragraphs) && article.paragraphs.length > 0
    ? article.paragraphs
    : String(article?.cleanedText || article?.text || '')
        .split(/\n\s*\n/) // 以空白行切段，容錯更高
        .filter(p => p.trim().length > 0);

  const renderResultSummary = () => {
    if (!result) return null;
    return (
      <Alert
        type="success"
        showIcon
        message={
          <Space wrap>
            <b>提交成功</b>
            <Tag color="gold">Correct: {result.correct}</Tag>
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
                      {/* 標題以問題內容呈現（粗體） */}
                      <b>Q{r.index + 1}. {r.prompt}</b>
                      {r.correct
                        ? <Tag color="green">✓ 正確</Tag>
                        : <Tag color="red">✗ 錯誤</Tag>}
                    </Space>
                  }
                />
                {/* 詳解不再重複印出題幹 */}
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
      {/* 文章區塊 */}
      <Card
        title={
          <Space>
            文章資訊
            {articleId && <Tag>文章ID: {articleId}</Tag>}
          </Space>
        }
        extra={
          <Space>
            {articleId && (
              <Link to={`/articles/${articleId}`}>
                <Button>查看全文</Button>
              </Link>
            )}
          </Space>
        }
      >
        {articleLoading && <Skeleton active paragraph={{ rows: 3 }} />}
        {!articleLoading && articleErr && <Text type="danger">{articleErr}</Text>}
        {!articleLoading && !articleErr && (
          <>
            <Descriptions column={1} size="small" bordered style={{ marginBottom: 12 }}>
              <Descriptions.Item label="來源">{article?.source || 'N/A'}</Descriptions.Item>
              <Descriptions.Item label="原文連結">
                {sourceLink ? (
                  <a href={sourceLink} target="_blank" rel="noreferrer">{sourceLink}</a>
                ) : '—'}
              </Descriptions.Item>
              {article?.title && (
                <Descriptions.Item label="標題">{article.title}</Descriptions.Item>
              )}
            </Descriptions>

            {/* ✅ 正確段落顯示：優先 paragraphs，否則用 cleanedText 分段 */}
            {paragraphs.length > 0 ? (
              paragraphs.map((p, i) => (
                <Paragraph key={i} style={{ lineHeight: 1.7, marginBottom: 16 }}>
                  {p}
                </Paragraph>
              ))
            ) : (
              <Paragraph>(無內容)</Paragraph>
            )}
          </>
        )}
      </Card>

      {/* 題組 + 直接作答 */}
      <Card
        title={
          <Space>
            題組 
            <Tag color="green">難度: {difficulty}</Tag>
          </Space>
        }
        extra={<Space><Link to="/exercises/generate"><Button>回產生頁</Button></Link></Space>}
      >
        <Form layout="vertical" onFinish={onSubmit}>
          {items.map((q, idx) => (
            <Form.Item key={idx} label={<b>Q{idx + 1}. {q.prompt}</b>} required>
              {/* 標題已呈現題幹，因此這裡不再重複印出 */}
              {Array.isArray(q.options) && q.options.length > 0 ? (
                // MCQ：value 為「選項索引 number」
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
