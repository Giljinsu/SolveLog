import logging
import sys
import time

from backend_client import BackendClient, BackendError
from comfyui_client import ComfyUIClient, ComfyUIError
from config import load_config

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger("worker.main")


def process_job(job, backend, comfyui):
    job_id = job["jobId"]
    prompt = job["prompt"]
    logger.info("Claimed job: %s", job_id)

    try:
        logger.info("Sending prompt to ComfyUI")
        image_bytes, filename, content_type = comfyui.generate_thumbnail(prompt)
        logger.info("Image downloaded")
    except ComfyUIError as e:
        message = _summarize(e)
        logger.error("ComfyUI generation failed for job %s: %s", job_id, message)
        backend.fail_job(job_id, message)
        return

    try:
        backend.complete_job(job_id, image_bytes, filename, content_type)
        logger.info("Complete succeeded")
    except BackendError as e:
        message = _summarize(e)
        logger.error("Complete failed for job %s: %s", job_id, message)
        backend.fail_job(job_id, message)


def _summarize(error, max_len=200):
    text = str(error)
    return text if len(text) <= max_len else text[:max_len] + "..."


def main():
    config = load_config()
    backend = BackendClient(config.backend_url, config.worker_token)
    comfyui = ComfyUIClient(config.comfyui_url, config.comfyui_generation_timeout)

    logger.info("Worker started")

    while True:
        logger.info("Claiming job...")
        try:
            job = backend.claim_job()
        except BackendError as e:
            logger.warning("Claim failed: %s", _summarize(e))
            time.sleep(config.poll_interval)
            continue

        if job is None:
            logger.info("No job available")
            time.sleep(config.poll_interval)
            continue

        process_job(job, backend, comfyui)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        logger.info("Worker stopped")
        sys.exit(0)
