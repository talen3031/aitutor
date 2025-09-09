# AITutor

一個 AI 輔助的線上學習平台，透過串接OpenAI API (GPT-4o-mini,GPT-4o-mini-TTS)，提供文章抓取、題目生成與作答功能。  
專案採用 **前後端分離架構**，包含：

- **後端 (backend)**：Java Spring Boot  / PostgreSQL / JPA
- **前端 (frontend)**：React / Axios / Ant Design
- **雲端部署(Railway)**: [aitutor](https://aitutor-frontend-production.up.railway.app)
---

### 系統架構圖

```text
[前端 React + Vite + Ant Design]
   │
   ├─ 文章管理：/api/articles/*
   ├─ 閱讀題目：/api/exercises/reading/*, /api/submissions/reading
   └─ 聽力題目：/api/exercises/listening/*, /api/submissions/listening
   │
   ▼
[Spring Boot REST API]
   ├── Article_Controller
   │       → ArticleService
   │       → ArticleRepository
   │       → [article 表]
   │
   ├── Exercise_Reading_Controller (閱讀題組)
   │       → Exercise_Reading_Service
   │       → Reading_Question_GenService
   │       → Reading_PromptFactory
   │       → LlmClient(OpenAiLlmClient) ──► [OpenAI GPT-4o-mini]
   │       → ExerciseSet_Reading_Repository
   │       → [exercise_set_readings 表]
   │
   ├── Exercise_Listening_Controller (聽力題組)
   │       → Exercise_Listening_Service
   │       → Listening_Question_GenService
   │       → Listening_PromptFactory                 
   │       → LlmClient(OpenAiLlmClient) ──► [OpenAI GPT-4o-mini]
   │       → OpenAiTtsClient ──────► [OpenAI TTS] ────► (物件儲存) Cloudflare R2 /audio/listening_*.mp3
   │       → ExerciseSet_Listening_Repository 
   │       → [exercise_set_listening 表 ]
   │
   ├── Reading_Submission_Controller (閱讀作答)
   │       → Reading_SubmissionService
   │       → Reading_SubmissionRepository
   │       → [submissions_reading 表]
   │
   └── Listening_Submission_Controller (聽力作答)
           → Listening_SubmissionService
           → Listening_SubmissionRepository
           → [submissions_listening 表]
   
  

   │
   ▼
[PostgreSQL]
   ├── article
   ├── exercise_set_reading
   ├── exercise_set_listening
   ├── submissions_reading
   └── submissions_listening

[外部服務]
   ├── JsoupFetcher ──► [新聞/文章來源網站]
   ├── OpenAiLlmClient ──► [OpenAI API: GPT-4o-mini]
   └── OpenAiTtsClient ──► [OpenAI API: GPT-4o-mini-TTS]

```
---

## ✨ 功能說明

### 文章管理
- 提供 API 可從外部新聞網站（例如 BBC）抓取文章。  
- 會自動清洗 HTML，過濾廣告、導覽、相關連結，只保留正文段落。  
- 文章會存入資料庫（PostgreSQL），並以內容雜湊去重。  
- 文章抓取流程:
```text
User
  │  (輸入文章網址)
  ▼
Frontend (React)
  │  POST /api/articles/fetch {url}
  ▼
ArticleController
  │
  ▼
ArticleService
  │──> JsoupFetcher → 外部新聞/Blog 網站 (抓 HTML)
  │──> 清洗內容、計算 content_hash
  │──> ArticleRepository 檢查是否已有相同文章
       │
       ├─ 已存在 → 回傳文章
       └─ 不存在 → INSERT INTO article
  ▼
回傳 {id, source, url, text(摘要)}
```
### 題目生成
- 串接OPENAI API (GPT4O-mini) 輸入網址文章後可以生成英文練習題目
- 依據文章內容，自動產生題目集（多選題、是非題等）。  
- 題目可依照 **難度**、**類型**、**數量**生成。  
- 每個題目集 (`ExerciseSet`) 與來源文章關聯。
- 生成題組流程:
```text
User
  │  (選擇文章 + 難度 + 題型數量)
  ▼
Frontend (React)
  │  POST /api/exercises/reading/generate 
  ▼
Exercise_Reading Controller
  │
  ▼
Exercise_Reading Service
  │──> 讀取 ArticleRepository (拿文章內容)
  │──> 建立 spec (難度/題型/數量)
  │──> ExerciseSetRepository 檢查是否已有相同題組
       │
       ├─ 已存在 → 回傳 exerciseSetId
       └─ 不存在 →
             │
             ▼
          QuestionGenService
             │──> PromptFactory (組 prompt)
             │──> LlmClient(OpenAiLlmClient)
             │──> OpenAI GPT-4o-mini 產生題目(JSON)
             │
             ▼
          ExerciseSetRepository.save(items)
  ▼
回傳 {exerciseSetId}

```
### 聽力功能
- 串接 **OPENAI API (GPT-4o-mini)** 產生聽力練習題目。  
- 系統會生成 **英文聽力稿 (transcript)**、對應的 **選擇題**，並透過 **TTS (Text-to-Speech)** 產生可播放的 MP3 音檔。  
- 支援題目難度、主題、體裁（對話 / 短文）設定。  
- 使用者可作答並提交，後端自動評分。  

- 聽力題組生成流程:
```text
User
  │  (選擇主題 + 難度 + 體裁 + 題數)
  ▼
Frontend (React)
  │  POST /api/exercises/listening/generate {difficulty, topic, genre, numQuestions}
  ▼
Exercise_ListeningController
  │
  ▼
Exercise_Listening Service
  │──> ListeningPromptFactory (組 prompt)
  │──> OpenAiLlmClient (呼叫 GPT-4o-mini 產生 transcript+questions JSON)
  │──> ListeningQuestionGenService 解析題目
  │──> OpenAiTtsClient (產生 mp3 語音檔，存放到 Cloudflare R2)
  │──> ExerciseSetListeningRepository.save()
  ▼
回傳 {exerciseSetId, transcript, audioUrl, questions[]}
```
### 作答與提交
- 使用者可直接在前端進行作答。  
- 提交答案後，後端會自動評分，並回傳詳細結果：  
  - 總分與正確數  
  - 每題正確與否  
  - 正確答案與使用者答案  
  - 解釋文字  
- 交卷評分流程:

```text
User
  │  (填完答案 → 提交)
  ▼
Frontend (React)
  │  POST /api/submission {exerciseSetId, answers}
  │  POST /api/submissions/listening {exerciseSetId, answers}
  ▼
ReadingSubmissionController / ListeningSubmissionController
  │
  ▼
SubmissionService
  │──> 讀取 ExerciseSetRepository (拿標準答案)
  │──> 比對使用者答案
  │──> 計算分數 (score, correct/total, 詳細結果)
  │──> SubmissionRepository.save(answers, score, detail)
  ▼
回傳 {submissionId, score, total, results[]}

```

---
## ⚙️ 環境需求

- **Java**: 21+
- **Node.js**: 18+ (建議使用 LTS)
- **npm**: 9+ / 或 yarn, pnpm
- **PostgreSQL**: 15+
---
