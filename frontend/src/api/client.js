import axios from 'axios';
import { message } from 'antd';

// 後端 baseURL：優先取 .env 的 VITE_API_BASE，否則預設 8080
// 例：在 .env.development 設 VITE_API_BASE=http://localhost:8080
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  // 統一給個合理的 timeout（也可在各別請求覆蓋）
  timeout: 20000,
});

// 統一錯誤處理
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg = err?.response?.data?.message || err.message || 'Request Error';
    message.error(msg);
    return Promise.reject(err);
  }
);
