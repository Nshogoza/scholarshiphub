# ---- Build stage -----------------------------------------------------------
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
# Built as a same-origin SPA behind Nginx (see nginx.conf), which proxies
# /api/v1/* to the backend container -- so the production bundle always
# calls the relative /api/v1 path regardless of VITE_API_BASE_URL.
ENV VITE_API_BASE_URL=/api/v1
RUN npm run build

# ---- Runtime stage ----------------------------------------------------------
FROM nginx:1.27-alpine AS runtime

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY proxy_params_scholarshiphub /etc/nginx/proxy_params_scholarshiphub

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://127.0.0.1/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
