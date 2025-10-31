ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

FROM node:22.12.0-alpine AS build
Label maintainer="therin234@gmail.com"
Label version="1.0.0"
Label description="A md blog Solvelog"
WORKDIR /app
COPY package*.json ./
RUN npm ci
# 앞 . 현재 디렉토리, 뒤 . 위의 WORKDIR인 /app이 됨
COPY . .
RUN npm run build

FROM nginx:1.28.0-alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
# 굳이 쓰는 명시적 의도로 적음
