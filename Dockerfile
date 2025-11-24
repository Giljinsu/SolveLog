#FROM gradle:8.13.0-jdk23 AS build
#WORKDIR /app
#COPY . .
#RUN ./gradlew build

# 개선버전
FROM gradle:8.13.0-jdk21 AS build
WORKDIR /app
#의존성 먼저 캐싱
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon
#실제 소스 복사 후 빌드
COPY . .
RUN ./gradlew build -x test --no-daemon #-x test 는 테스트 실행 X

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
