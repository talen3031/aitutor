# ====== Build stage ======
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# 若需要在 build 時帶入 API 位址：
#   npm run build -- --base=/  依你專案腳本調整
RUN npm run build

# ====== Runtime stage (Nginx) ======
FROM nginx:alpine
# Nginx SPA 設定：所有路由回 index.html（避免 F5 404）
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://127.0.0.1/ >/dev/null || exit 1
