import React,{ useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Card, Form, Radio, Input, Button, Space, Typography, Alert, Skeleton, Tag, List } from 'antd';
import { getExerciseSet } from '../api/reading_exercises.js';
import { submitAnswers } from '../api/reading_submissions.js';

const { Text } = Typography;

export default function SubmissionForm() {
  const { exerciseSetId } = useParams();
  const [ex, setEx] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null); // 後端回傳的評分結果

  // 讀題組
  useEffect(() => {
    (async () => {
      try {
        setErr('');
        setLoading(true);
        const data = await getExerciseSet(exerciseSetId);
        setEx(data);
        setResult(null);
      } catch (e) {
        setErr(e?.response?.data?.message || e.message || '讀取失敗');
      } finally {
        setLoading(false);
      }
    })();
  }, [exerciseSetId]);

  // 初始化作答：選擇題存「索引 number」，其他題型存字串
  const initAnswers = useMemo(() => {
    if (!ex || !Array.isArray(ex.items)) return {};
    const ans = {};
    ex.items.forEach((q, idx) => {
      ans[idx] = Array.isArray(q?.options) ? null : '';
    });
    return ans;
  }, [ex]);

  const [answers, setAnswers] = useState({});
  useEffect(() => setAnswers(initAnswers), [initAnswers]);

  // 送出：符合你指定的 payload
  const onSubmit = async () => {
    setErr('');
    setSubmitting(true);
    try {
      const responses = (ex.items || []).map((q, idx) => {
        const a = answers[idx];
        if (Array.isArray(q?.options)) {
          // 單選題以「選項索引」送出；未作答以 -1（若後端不接受，換你要的預設）
          return { index: idx, answer: (a === null || a === undefined) ? -1 : Number(a) };
        }
        return { index: idx, answer: a ?? '' };
      });

      const payload = {
        exerciseSetId: Number(exerciseSetId),
        responses
      };

      const data = await submitAnswers(payload);
      setResult(data); // { score,total,submissionId,correct,exerciseSetId,results:[...] }
    } catch (e) {
      setErr(e?.response?.data?.message || e.message || '提交失敗');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Skeleton active />;
  if (err) return <Card><Text type="danger">{err}</Text></Card>;
  if (!ex) return <Card>找不到題組</Card>;

  const renderResultSummary = () => {
    if (!result) return null;
    return (
      <Alert
        type="success"
        showIcon
        message={
          <Space wrap>
            <b>提交成功</b>
            <Tag color="blue">Submission ID: {result.submissionId}</Tag>
            <Tag color="green">Score: {result.score} / {result.total}</Tag>
            <Tag color="gold">Correct: {result.correct}</Tag>
            <Tag>Set: #{result.exerciseSetId}</Tag>
          </Space>
        }
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
                      <b>Q{r.index + 1}. {r.type?.toUpperCase() || 'QUESTION'}</b>
                      {r.correct
                        ? <Tag color="green">✓ 正確</Tag>
                        : <Tag color="red">✗ 錯誤</Tag>}
                    </Space>
                  }
                />
                <div style={{ whiteSpace: 'pre-wrap', marginBottom: 8 }}>{r.prompt}</div>

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
    <Card title={`作答並交卷`}>
      <Form layout="vertical" onFinish={onSubmit}>
        {ex.items?.map((q, idx) => (
          <Form.Item
            key={idx}
            label={<b>Q{idx + 1}. {q.type?.toUpperCase() || 'QUESTION'}</b>}
            required
          >
            <div style={{ whiteSpace: 'pre-wrap', marginBottom: 8 }}>{q.prompt}</div>

            {Array.isArray(q.options) && q.options.length > 0 ? (
              // 單選題：value = 選項索引（number）
              <Radio.Group
                onChange={e => setAnswers(prev => ({ ...prev, [idx]: Number(e.target.value) }))}
                value={answers[idx]}
                style={{ display: 'grid', gap: 6 }}
              >
                {q.options.map((opt, i) => (
                  <Radio key={i} value={i}>{opt}</Radio>
                ))}
              </Radio.Group>
              ) : q.type === 'tf' ? (
              <Radio.Group
                onChange={(e) => setAnswers(prev => ({ ...prev, [idx]: e.target.value }))} // 直接存 boolean
                value={answers[idx]} // 讀取 boolean
                style={{ display: 'flex', gap: 12 }}
              >
                <Radio value={true}>True</Radio>
                <Radio value={false}>False</Radio>
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
          {err && <Alert type="error" showIcon message={err} />}
        </Space>
      </Form>

      {/* 成功摘要 + 逐題詳解 */}
      <div style={{ marginTop: 16 }}>
        {renderResultSummary()}
        {renderPerQuestion()}
      </div>

      {result && (
        <div style={{ marginTop: 16 }}>
          <Link to={`/exercises/${ex.id}`}><Button>回題組</Button></Link>
        </div>
      )}
    </Card>
  );
}
