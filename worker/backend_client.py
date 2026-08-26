import logging
import time

import requests

logger = logging.getLogger("worker.backend")

COMPLETE_MAX_ATTEMPTS = 3
COMPLETE_RETRY_DELAY_SECONDS = 2


class BackendError(Exception):
    pass


class BackendClient:
    def __init__(self, base_url, worker_token):
        self.base_url = base_url
        self._headers = {"Authorization": f"Bearer {worker_token}"}

    def claim_job(self):
        """Job이 없으면 None, 있으면 {"jobId": ..., "prompt": ...} dict 반환."""
        url = f"{self.base_url}/api/internal/thumbnail-jobs/claim"
        try:
            resp = requests.post(url, headers=self._headers, timeout=10)
        except requests.RequestException as e:
            raise BackendError(f"claim request failed: {e}") from e

        if resp.status_code == 204:
            return None
        if resp.status_code == 200:
            return resp.json()
        raise BackendError(f"claim failed with status {resp.status_code}")

    def complete_job(self, job_id, image_bytes, filename, content_type):
        """성공(200) 또는 409(이미 완료된 것으로 간주)면 정상 반환. 그 외는 BackendError."""
        url = f"{self.base_url}/api/internal/thumbnail-jobs/{job_id}/complete"

        last_error = None
        for attempt in range(1, COMPLETE_MAX_ATTEMPTS + 1):
            files = {"image": (filename, image_bytes, content_type)}
            try:
                resp = requests.post(url, headers=self._headers, files=files, timeout=30)
            except requests.RequestException as e:
                last_error = e
                logger.warning(
                    "complete request error (attempt %d/%d): %s",
                    attempt, COMPLETE_MAX_ATTEMPTS, e,
                )
                if attempt < COMPLETE_MAX_ATTEMPTS:
                    time.sleep(COMPLETE_RETRY_DELAY_SECONDS)
                continue

            if resp.status_code == 200:
                return

            if resp.status_code == 409:
                logger.warning(
                    "complete got 409 for job %s - treating as already completed", job_id
                )
                return

            if 500 <= resp.status_code < 600:
                last_error = BackendError(f"complete failed with status {resp.status_code}")
                logger.warning(
                    "complete server error (attempt %d/%d): status=%s",
                    attempt, COMPLETE_MAX_ATTEMPTS, resp.status_code,
                )
                if attempt < COMPLETE_MAX_ATTEMPTS:
                    time.sleep(COMPLETE_RETRY_DELAY_SECONDS)
                continue

            # 409를 제외한 4xx는 재시도하지 않는다.
            raise BackendError(f"complete failed with status {resp.status_code}")

        raise BackendError(f"complete failed after {COMPLETE_MAX_ATTEMPTS} attempts: {last_error}")

    def fail_job(self, job_id, message):
        """
        200이면 정상 종료. 409면 이미 종결된 Job으로 보고 조용히 넘어간다.
        그 외 실패는 경고 로그만 남기고 예외를 던지지 않는다 - 메인 루프는 계속 진행돼야 한다.
        """
        url = f"{self.base_url}/api/internal/thumbnail-jobs/{job_id}/fail"
        body = {"message": message or "unknown error"}

        try:
            resp = requests.post(url, headers=self._headers, json=body, timeout=10)
        except requests.RequestException as e:
            logger.warning("fail request error for job %s: %s", job_id, e)
            return

        if resp.status_code == 200:
            logger.info("Fail sent for job %s", job_id)
            return
        if resp.status_code == 409:
            logger.info("fail got 409 for job %s - already finalized, ignoring", job_id)
            return

        logger.warning(
            "fail request unexpected status %s for job %s", resp.status_code, job_id
        )
