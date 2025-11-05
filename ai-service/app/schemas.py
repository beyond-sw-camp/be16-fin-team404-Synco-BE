from pydantic import BaseModel
from typing import List, Optional, Dict

class Segment(BaseModel):
    start: float
    end: float
    speaker: str
    text: str

class ProcessResponse(BaseModel):
    meetingId: str
    text: str
    segments: List[Segment]
    saved: Optional[Dict[str, str]] = None

# Kafka 이벤트 DTO
class RecordingCompletedEvent(BaseModel):
    recordingSeq: int
    egressId: str
    outputUrl: str
    filename: str
    roomSeq: int

class TranscriptEvent(BaseModel):
    recordingSeq: int
    transcript: str
    outputUrl: str
