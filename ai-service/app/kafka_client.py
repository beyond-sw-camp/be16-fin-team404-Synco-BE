import json
import os
import time
import requests
import logging
import tempfile
from typing import Dict, Any
from kafka import KafkaProducer, KafkaConsumer
from kafka.errors import KafkaError
from urllib.parse import urlparse

logger = logging.getLogger(__name__)

class STTKafkaClient:
    def __init__(self):
        self.bootstrap_servers = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
        self.request_topic = os.getenv("KAFKA_REQUEST_TOPIC", "recording-completed")
        self.response_topic = os.getenv("KAFKA_RESPONSE_TOPIC", "transcript-completed")

        self.producer = KafkaProducer(
            bootstrap_servers=self.bootstrap_servers,
            key_serializer=lambda k: k.encode('utf-8') if k else None,  # Key 직렬화 추가
            value_serializer=lambda v: json.dumps(v).encode('utf-8'),  # Value JSON 직렬화
            retries=3,
            retry_backoff_ms=1000
        )

        self.consumer = KafkaConsumer(
            self.request_topic,
            bootstrap_servers=self.bootstrap_servers,
            value_deserializer=lambda m: json.loads(m.decode('utf-8')),
            group_id='stt-service-group',
            auto_offset_reset='latest',
            enable_auto_commit=False,  # 수동 commit 사용
            max_poll_interval_ms=1800000,  # 30분 (STT 처리 시간 고려)
            max_poll_records=1,  # 한 번에 하나씩만 처리
            session_timeout_ms=30000,  # 30초
            heartbeat_interval_ms=10000  # 10초마다 heartbeat
        )

    def download_file(self, url: str, save_path: str) -> bool:
        """S3 URL 또는 HTTP URL에서 파일 다운로드"""
        try:
            parsed_url = urlparse(url)
            
            # S3 URL인 경우 boto3 사용 가능 여부 확인
            if parsed_url.netloc.endswith('.s3.amazonaws.com') or 's3' in parsed_url.scheme:
                # boto3를 사용한 다운로드 시도 (선택적)
                try:
                    import boto3
                    s3_client = boto3.client('s3')
                    
                    # URL에서 bucket과 key 추출
                    if '.s3.amazonaws.com' in parsed_url.netloc:
                        bucket = parsed_url.netloc.split('.s3.amazonaws.com')[0]
                        key = parsed_url.path.lstrip('/')
                    else:
                        # s3://bucket/key 형태
                        bucket = parsed_url.netloc
                        key = parsed_url.path.lstrip('/')
                    
                    logger.info(f"S3 다운로드 시도: bucket={bucket}, key={key}")
                    s3_client.download_file(bucket, key, save_path)
                    logger.info(f"S3 다운로드 완료: {save_path}")
                    return True
                except ImportError:
                    logger.info("boto3가 설치되지 않음. requests로 시도합니다.")
                except Exception as e:
                    logger.warning(f"S3 다운로드 실패: {e}. requests로 재시도합니다.")
            
            # 일반 HTTP(S) 다운로드 또는 S3 실패 시 대체
            logger.info(f"HTTP 다운로드 시도: {url}")
            response = requests.get(url, stream=True, timeout=60)
            response.raise_for_status()
            
            with open(save_path, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    f.write(chunk)
            
            logger.info(f"파일 다운로드 완료: {save_path}")
            return True
        except Exception as e:
            logger.error(f"파일 다운로드 실패: {e}")
            return False

    def send_result(self, recording_seq: int, transcript: str, output_url: str):
        """STT 처리 결과 전송"""
        message = {
            "recordingSeq": recording_seq,
            "transcript": transcript,
            "outputUrl": output_url
        }

        try:
            # Spring Boot Kafka 설정에 맞춰 Key 추가 (recordingSeq.toString())
            future = self.producer.send(
                self.response_topic,
                key=str(recording_seq),  # Key 추가
                value=message
            )
            
            # 전송 완료 대기 및 결과 확인
            record_metadata = future.get(timeout=10)
            logger.info(f"✅ STT 결과 전송 완료: recordingSeq={recording_seq}, topic={record_metadata.topic}, partition={record_metadata.partition}, offset={record_metadata.offset}")
            
            self.producer.flush()
        except KafkaError as e:
            logger.error(f"❌ STT 결과 전송 실패 (KafkaError): {e}", exc_info=True)
        except Exception as e:
            logger.error(f"❌ STT 결과 전송 실패 (Exception): {e}", exc_info=True)

    def consume_requests(self):
        """recording-completed 이벤트 수신 및 STT 처리"""
        logger.info(f"Kafka Consumer 시작: {self.request_topic}")
        
        for message in self.consumer:
            try:
                event = message.value
                recording_seq = event.get("recordingSeq")
                output_url = event.get("outputUrl")
                filename = event.get("filename")

                logger.info(f"STT 요청 수신: recordingSeq={recording_seq}, url={output_url}")

                # 임시 파일 생성 (filename에서 확장자 추출)
                file_extension = os.path.splitext(filename)[1] if filename else ".mp4"
                # 경로가 포함된 경우 파일명만 추출
                if filename and '/' in filename:
                    filename_basename = os.path.basename(filename)
                else:
                    filename_basename = filename or "temp"
                
                # 임시 파일 경로 생성
                tmp_path = os.path.join(tempfile.gettempdir(), f"stt_{recording_seq}_{filename_basename}")
                
                if not self.download_file(output_url, tmp_path):
                    logger.error(f"파일 다운로드 실패: recordingSeq={recording_seq}")
                    # 다운로드 실패 시에도 commit하여 중복 처리 방지
                    self.consumer.commit()
                    continue

                # STT 처리
                from .asr_core import run_diar_asr
                result = run_diar_asr(tmp_path)

                # STT 결과 출력
                print(f"\n{'='*80}")
                print(f"✅ STT 처리 완료 - recordingSeq: {recording_seq}")
                print(f"{'='*80}")
                print(f"📝 전사 결과:")
                print(f"{result['text'][:200]}..." if len(result['text']) > 200 else result['text'])
                print(f"\n📊 통계:")
                print(f"  - 세그먼트 수: {len(result['segments'])}")
                print(f"  - 전체 텍스트 길이: {len(result['text'])}자")
                print(f"{'='*80}\n")

                # 결과 전송
                self.send_result(
                    recording_seq=recording_seq,
                    transcript=result["text"],
                    output_url=output_url
                )

                # 임시 파일 정리
                try:
                    os.unlink(tmp_path)
                except Exception as e:
                    logger.warning(f"임시 파일 삭제 실패: {e}")

                # 처리 완료 후 수동 commit
                self.consumer.commit()
                logger.info(f"✅ Offset commit 완료: recordingSeq={recording_seq}")

            except Exception as e:
                logger.error(f"STT 처리 중 오류 발생: {e}", exc_info=True)
                # 오류 발생 시에도 commit하여 무한 재시도 방지 (선택적)
                # 필요시 DLQ(Dead Letter Queue)로 전송하는 로직 추가 가능
                try:
                    self.consumer.commit()
                    logger.warning(f"오류 발생 후 commit 완료: recordingSeq={recording_seq}")
                except Exception as commit_error:
                    logger.error(f"Commit 실패: {commit_error}", exc_info=True)
