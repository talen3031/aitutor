// src/App.jsx
import React,{ useMemo } from 'react';
import { Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import { ConfigProvider, Layout, Menu, theme } from 'antd';
import { FileTextOutlined, ExperimentOutlined, CustomerServiceOutlined } from '@ant-design/icons';

import ArticleFetcher from './components/ArticleFetcher.jsx';
import ArticleViewer from './components/ArticleViewer.jsx';
import ExerciseGenerator from './components/ExerciseGenerator.jsx';
import ExerciseViewer from './components/ExerciseViewer.jsx';
import SubmissionForm from './components/SubmissionForm.jsx';
import ArticleList from './components/ArticleList.jsx';

// ⬇️ 新增：英聽頁面
import ListeningList from './components/ListeningList.jsx';
import ListeningGenerator from './components/ListeningGenerator.jsx';
import ListeningSolve from './components/ListeningSolve.jsx';

const { Header, Content, Footer } = Layout;

export default function App() {
  const loc = useLocation();
  const nav = useNavigate();

  // ✅ 讓選單能正確高亮（含新加入的 /listening 路徑）
  const selected = useMemo(() => {
    if (loc.pathname.startsWith('/articles')) return ['/articles'];
    if (loc.pathname === '/' || loc.pathname.startsWith('/articles')) return ['/articles'];
    if (loc.pathname.startsWith('/exercises/generate')) return ['/exercises/generate'];
    if (loc.pathname.startsWith('/exercises/')) return ['/exercises'];
    if (loc.pathname.startsWith('/submit/')) return ['/submit'];
    if (loc.pathname.startsWith('/listening/generate')) return ['/listening/generate'];
    if (loc.pathname.startsWith('/listening')) return ['/listening'];
    return ['/articlefetcher'];
  }, [loc.pathname]);

  const items = [
    { key: '/articles', icon: <FileTextOutlined />, label: <Link to="/articles">文章列表</Link> },
    { key: '/articlefetcher', icon: <FileTextOutlined />, label: <Link to="/articlefetcher">產生閱讀題組</Link> },
    { key: '/listening', icon: <CustomerServiceOutlined />, label: <Link to="/listening">英聽題庫</Link> },
    { key: '/listening/generate', icon: <CustomerServiceOutlined />, label: <Link to="/listening/generate">產生英聽題組</Link> },
  ];

  return (
    <ConfigProvider theme={{ algorithm: theme.defaultAlgorithm, token: { colorPrimary: '#1d7dfa', borderRadius: 10 } }}>
      <Layout style={{ minHeight: '100vh' }}>
        <Header style={{ display: 'flex', alignItems: 'center' }}>
          <div onClick={() => nav('/')} style={{ color: 'white', fontWeight: 700, marginRight: 24, cursor: 'pointer' }}>
            AI Tutor
          </div>
          <Menu theme="dark" mode="horizontal" selectedKeys={selected} items={items} style={{ flex: 1, minWidth: 0 }} />
        </Header>

        <Content className="container" style={{ padding: 24 }}>
          <Routes>
            {/* ✅ 首頁直接顯示ArticleList */}
            <Route path="/" element={<ArticleList />} />
            {/* ✅ 也保留 /articlefetcher 路徑 */}
            <Route path="/articlefetcher" element={<ArticleFetcher />} />

            <Route path="/articles" element={<ArticleList />} />
            <Route path="/articles/:id" element={<ArticleViewer />} />

            <Route path="/exercises/generate" element={<ExerciseGenerator />} />
            <Route path="/exercises/:id" element={<ExerciseViewer />} />

            <Route path="/submit/:exerciseSetId" element={<SubmissionForm />} />

            {/* ⬇️ 新增：英聽三頁 */}
            <Route path="/listening" element={<ListeningList />} />
            <Route path="/listening/generate" element={<ListeningGenerator />} />
            <Route path="/listening/:id" element={<ListeningSolve />} />

            <Route path="*" element={<div>404 Not Found</div>} />
          </Routes>
        </Content>

        <Footer style={{ textAlign: 'center' }}>
          AI Tutor Demo © {new Date().getFullYear()}
        </Footer>
      </Layout>
    </ConfigProvider>
  );
}
