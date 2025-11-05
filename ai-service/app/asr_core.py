import os
from typing import Dict, List
from faster_whisper import WhisperModel
from .utils import to_wav_16k_mono

# 환경변수 설정
ASR_MODEL = os.getenv("ASR_MODEL", "medium")
ASR_COMPUTE_TYPE = os.getenv("ASR_COMPUTE_TYPE", "int8")  # CPU 최적화
ASR_DEVICE = os.getenv("ASR_DEVICE", "cpu")  # CPU 전용

# 모델 로딩
def load_asr_model() -> WhisperModel:
    """
    CPU 최적화된 ASR 모델 로딩
    """
    print(f"ASR 모델 로딩: {ASR_MODEL}, 디바이스: {ASR_DEVICE}, 타입: {ASR_COMPUTE_TYPE}")
    
    return WhisperModel(
        ASR_MODEL, 
        device=ASR_DEVICE,
        compute_type=ASR_COMPUTE_TYPE,
        download_root=None,
        local_files_only=False
    )

ASR = load_asr_model()

# STT 처리 함수
def run_asr_only(input_path: str) -> Dict:
    """
    음성 추출 (화자분리 없음)
    input_path: 원본 비디오/오디오 파일 경로
    return: {"segments":[{start,end,speaker,text},...], "text":"..."}
    """
    import time
    start_time = time.time()
    
    # 오디오 표준화
    print("오디오 표준화 중...")
    wav_path = to_wav_16k_mono(input_path)
    audio_time = time.time() - start_time
    print(f"오디오 표준화 완료: {audio_time:.2f}초")
    
    # ASR 수행
    print("ASR 처리 중...")
    asr_start = time.time()
    
    segs, info = ASR.transcribe(
        wav_path, 
        language="ko", 
        vad_filter=True,
        beam_size=5,
        best_of=5,
        temperature=0.0,
        condition_on_previous_text=True,
        word_timestamps=True,
        initial_prompt="회의 내용을 정확하게 전사해주세요.",
    )
    
    asr_time = time.time() - asr_start
    print(f"ASR 처리 완료: {asr_time:.2f}초")
    
    # 결과 정리
    out: List[Dict] = []
    full_text_parts = []
    
    for seg in segs:
        speaker = "Speaker"
        out.append({
            "start": float(seg.start), 
            "end": float(seg.end), 
            "speaker": speaker, 
            "text": seg.text.strip()
        })
        full_text_parts.append(seg.text.strip())
    
    full_text = " ".join(full_text_parts)
    
    total_time = time.time() - start_time
    print(f"=== ASR 성능 통계 ===")
    print(f"오디오 표준화: {audio_time:.2f}초")
    print(f"ASR 처리: {asr_time:.2f}초")
    print(f"총 처리 시간: {total_time:.2f}초")
    print(f"세그먼트 수: {len(out)}")
    
    return {"segments": out, "text": full_text}

# 메인 STT 함수
def run_diar_asr(input_path: str) -> Dict:
    return run_asr_only(input_path)
