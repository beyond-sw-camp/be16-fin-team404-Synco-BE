FROM python:3.11-slim

# 시스템 패키지 설치 (ffmpeg 등)
RUN apt-get update && apt-get install -y \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# Python 의존성 파일 복사
COPY requirements.txt .

# Python 패키지 설치
# torch는 CPU 전용으로 별도 설치
RUN pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu && \
    pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# 환경변수 설정
ENV PYTHONUNBUFFERED=1

# 애플리케이션 실행
CMD ["python", "main.py"]

