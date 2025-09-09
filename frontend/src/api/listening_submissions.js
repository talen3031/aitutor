// src/api/listening_submissions.js
import { api } from './client';

function request(path, { method = 'GET', data, params, headers, timeout } = {}) {
  return api.request({
    url: path,
    method,
    data,
    params: { ...(params || {}), _ts: Date.now() },
    headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache', ...(headers || {}) },
    timeout: timeout ?? 30000,
  }).then(res => res.data);
}

// A) 提交為 answers: number[]（MCQ用索引；其他題型依後端規格）
export function submitListeningAnswers(exerciseSetId, answers) {
  // POST /api/submissions/listening
  return request('/api/submissions/listening', {
    method: 'POST',
    data: { exerciseSetId, answers },
  });
}

// B) 若你使用 responses 物件陣列版本（{index, answer}[]），用這個
export function submitListeningResponses(exerciseSetId, responses) {
  // POST /api/submissions/listening
  return request('/api/submissions/listening', {
    method: 'POST',
    data: { exerciseSetId, responses },
  });
}
