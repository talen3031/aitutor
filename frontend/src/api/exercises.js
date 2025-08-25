// src/api/exercises.js
import { api } from './client';

// 讀取題組
export async function getExerciseSet(id) {
  const { data } = await api.get(`/api/exercises/${id}`, {
    headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache' },
    params: { _ts: Date.now() },
    timeout: 15000,
  });
  return data;
}

// 產生題組
export async function generateExerciseSet(payload) {
  const { data } = await api.post('/api/exercises/generate', payload, {
    headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache' },
    params: { _ts: Date.now() },
    timeout: 30000,
  });
  return data;
}
