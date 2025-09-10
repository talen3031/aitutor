// src/api/listening_exercises.js
import { api } from './client';

function request(path, { method = 'GET', data, params, headers, timeout } = {}) {
  return api.request({
    url: path,
    method,
    data,
    params: { ...(params || {}), _ts: Date.now() }, // 防快取
    headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache', ...(headers || {}) },
    timeout: timeout ?? 30000,
  }).then(res => res.data);
}

export function listListeningExercises() {
  // GET /api/exercises/listening
  return request('/api/exercises/listening', { method: 'GET', timeout: 15000 })
    .then((data) => (Array.isArray(data) ? data : data?.content ?? []));
}

export function generateListeningExercise({ difficulty, numQuestions, topics, genre }) {
  // POST /api/exercises/listening/generate
  return request('/api/exercises/listening/generate', {
    method: 'POST',
    data: { difficulty, numQuestions, topics, genre },
  });
}

export function getListeningExercise(id) {
  // GET /api/exercises/listening/{id}
  return request(`/api/exercises/listening/${id}`, { method: 'GET', timeout: 15000 });
}
