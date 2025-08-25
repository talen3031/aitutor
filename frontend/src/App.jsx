// src/App.jsx
import React,{ useMemo } from 'react';
import { Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import { ConfigProvider, Layout, Menu, theme } from 'antd';
import { FileTextOutlined, ExperimentOutlined } from '@ant-design/icons';

import ArticleFetcher from './components/ArticleFetcher.jsx';
import ArticleViewer from './components/ArticleViewer.jsx';
import ExerciseGenerator from './components/ExerciseGenerator.jsx';
import ExerciseViewer from './components/ExerciseViewer.jsx';
import SubmissionForm from './components/SubmissionForm.jsx';
import ArticleList from './components/ArticleList.jsx';

const { Header, Content, Footer } = Layout;

export default function App() {
  const loc = useLocation();
  const nav = useNavigate();

  // ✅ 讓首頁("/")與/articlefetcher 都選中「抓文章」
  const selected = useMemo(() => {
    if (loc.pathname.startsWith('/articles')) return ['/articles'];
    if (loc.pathname === '/' || loc.pathname.startsWith('/articles')) return ['/articles'];
    if (loc.pathname.startsWith('/exercises/generate')) return ['/exercises/generate'];
    if (loc.pathname.startsWith('/exercises/')) return ['/exercises'];
    if (loc.pathname.startsWith('/submit/')) return ['/submit'];
    return ['/articlefetcher'];
  }, [loc.pathname]);

  const items = [
    { key: '/articlefetcher', icon: <FileTextOutlined />, label: <Link to="/articlefetcher">抓文章</Link> },
    { key: '/articles', icon: <FileTextOutlined />, label: <Link to="/articles">文章列表</Link> },
    { key: '/exercises/generate', icon: <ExperimentOutlined />, label: <Link to="/exercises/generate">產生題組</Link> }
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
