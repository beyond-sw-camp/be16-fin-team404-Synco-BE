import os, subprocess, tempfile, uuid, shutil
from typing import List, Dict

def ensure_ffmpeg():
    try:
        subprocess.run(["ffmpeg", "-version"], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except Exception:
        raise RuntimeError("ffmpeg가 필요합니다. (macOS: brew install ffmpeg / Ubuntu: apt-get install -y ffmpeg)")

def to_wav_16k_mono(src_path: str) -> str:
    ensure_ffmpeg()
    dst_path = os.path.join(tempfile.gettempdir(), f"{uuid.uuid4().hex}.wav")
    subprocess.run(["ffmpeg", "-y", "-i", src_path, "-ac", "1", "-ar", "16000", dst_path],
                   check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return dst_path

def to_srt(items: List[Dict]) -> str:
    def fmt(t: float) -> str:
        h = int(t // 3600); m = int((t % 3600) // 60); s = int(t % 60)
        ms = int(round((t - int(t)) * 1000))
        return f"{h:02d}:{m:02d}:{s:02d},{ms:03d}"
    lines = []
    for i, it in enumerate(items, 1):
        lines.append(str(i))
        lines.append(f"{fmt(it['start'])} --> {fmt(it['end'])}")
        spk = (it.get("speaker") or "").strip()
        prefix = f"{spk}: " if spk else ""
        lines.append(prefix + (it.get("text") or "").strip())
        lines.append("")
    return "\n".join(lines)

def mktemp_dir() -> str:
    d = os.path.join(tempfile.gettempdir(), uuid.uuid4().hex)
    os.makedirs(d, exist_ok=True)
    return d

def cleanup(path: str):
    try:
        shutil.rmtree(path, ignore_errors=True)
    except Exception:
        pass
