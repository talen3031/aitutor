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

// ✅ 新增：列表 API（前端用 1-based，後端用 0-based）
export async function listArticles({ page = 1, size = 12 } = {}) {
  const backendPage = Math.max(0, Number(page) - 1);
  const { data } = await api.get('/api/articles', {
    headers: { 'Cache-Control': 'no-cache', Pragma: 'no-cache' },
    params: { page: backendPage, size, _ts: Date.now() }
  });
  // 從後端 Page 物件取出需要的欄位，統一成前端易用格式
  return {
    items: data?.content ?? [],
    total: data?.totalElements ?? 0,
    page: (data?.page ?? backendPage) + 1, // 轉回 1-based
    size: data?.size ?? size,
    totalPages: data?.totalPages ?? 1,
  };
}