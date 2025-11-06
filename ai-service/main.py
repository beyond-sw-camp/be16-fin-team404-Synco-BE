# main.py - Kafka Consumer 기반 STT 서비스
from dotenv import load_dotenv
load_dotenv()

import logging
from app.kafka_client import STTKafkaClient

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

def main():
    logger.info("=== STT Kafka Consumer 서비스 시작 ===")
    
    kafka_client = STTKafkaClient()
    kafka_client.consume_requests()

if __name__ == "__main__":
    main()