# ===== Build stage =====
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_API_BASE
ENV VITE_API_BASE=$VITE_API_BASE
RUN npm run build

# ===== Run stage =====
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80

# 在容器啟動前修改 default.conf，加上 SPA fallback
RUN sed -i '/location \/ {/a \ \ \ \ try_files $uri /index.html;' /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]
