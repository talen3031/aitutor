import { api } from './client';

export async function fetchArticleByUrl(url) {
  const { data } = await api.post('/api/articles/fetch', { url }, {
    headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache' },
    params: { _ts: Date.now() }
  });
  return data;
}

export async function getArticle(id) {
  const { data } = await api.get(`/api/articles/${id}`, {
    headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache' },
    params: { _ts: Date.now() }
  });
  return data;
}
