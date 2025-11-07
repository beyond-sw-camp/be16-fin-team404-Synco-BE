FROM eclipse-temurin:17-jdk-jammy AS stage1
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
RUN chmod +x gradlew
RUN ./gradlew bootJar

# 두번째 스테이지
# 이미지 경량화를 위해 스테이지 분리
FROM eclipse-temurin:17-jdk-jammy
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*
WORKDIR /app
# stage1의 jar파일을 stage2로 copy
COPY --from=stage1 /app/build/libs/*.jar app.jar
# 실행 : CMD 또는 ENTRYPOINT를 통해 컨테이너 실행
ENTRYPOINT [ "java", "-jar", "app.jar" ]

