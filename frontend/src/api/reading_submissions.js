import { api } from './client';

// 依你要求：POST /api/submission（單數）
export async function submitAnswers(payload) {
  const { data } = await api.post('/api/submission/reading', payload, {
    headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache' },
    params: { _ts: Date.now() },
    timeout: 30000
  });
  return data;
}
