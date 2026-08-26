import os
import sys
from pathlib import Path

from dotenv import load_dotenv

CONFIG_DIR = Path(__file__).resolve().parent
VALID_ENVS = ("local", "prod")
DEFAULT_ENV = "local"


class Config:
    def __init__(self):
        self.env = _resolve_env()
        _load_dotenv_for_env(self.env)
        print(f"[INFO] Worker environment: {self.env}")

        self.backend_url = _require_env("BACKEND_URL").rstrip("/")
        self.worker_token = _require_env("WORKER_TOKEN")
        self.comfyui_url = _require_env("COMFYUI_URL").rstrip("/")
        self.poll_interval = _positive_float_env("POLL_INTERVAL", 2)
        self.comfyui_generation_timeout = _positive_float_env(
            "COMFYUI_GENERATION_TIMEOUT", 120
        )


def _resolve_env():
    env = os.environ.get("ENV", DEFAULT_ENV)
    if env not in VALID_ENVS:
        print(
            f"[FATAL] ENV must be one of {VALID_ENVS}, got: {env!r}", file=sys.stderr
        )
        sys.exit(1)
    return env


def _load_dotenv_for_env(env):
    # 이미 설정된 OS 환경변수를 덮어쓰지 않는다 (override=False가 기본값이지만 명시).
    # 파일이 없으면 조용히 넘어가고, 이후 _require_env가 실제 누락 여부를 검증한다.
    dotenv_path = CONFIG_DIR / f".env.{env}"
    load_dotenv(dotenv_path=dotenv_path, override=False)


def _require_env(name):
    value = os.environ.get(name)
    if not value:
        print(f"[FATAL] required environment variable missing: {name}", file=sys.stderr)
        sys.exit(1)
    return value


def _positive_float_env(name, default):
    raw = os.environ.get(name, str(default))
    try:
        value = float(raw)
    except ValueError:
        print(f"[FATAL] {name} must be a number, got: {raw!r}", file=sys.stderr)
        sys.exit(1)

    if value <= 0:
        print(f"[FATAL] {name} must be greater than 0, got: {value}", file=sys.stderr)
        sys.exit(1)

    return value


def load_config():
    return Config()
