import copy
import json
import logging
import random
import time
from pathlib import Path

import requests

logger = logging.getLogger("worker.comfyui")

POSITIVE_PROMPT_NODE_ID = "6"
KSAMPLER_NODE_ID = "3"
SAVE_IMAGE_NODE_ID = "9"

HISTORY_POLL_INTERVAL_SECONDS = 1
MIN_SEED = 1
MAX_SEED = 2 ** 63 - 1

WORKFLOW_PATH = Path(__file__).resolve().parent / "workflow" / "sdxl_lightning.json"


class ComfyUIError(Exception):
    pass


class ComfyUIClient:
    def __init__(self, base_url, generation_timeout):
        self.base_url = base_url
        self.generation_timeout = generation_timeout
        self._workflow_template = _load_workflow_template()

    def generate_thumbnail(self, prompt):
        """
        Job마다 template을 deepcopy 해서 positive prompt/seed만 주입한다.
        Sampler 설정, negative prompt, image size는 절대 건드리지 않는다.
        반환값: (image_bytes, filename, content_type)
        """
        workflow = copy.deepcopy(self._workflow_template)
        workflow[POSITIVE_PROMPT_NODE_ID]["inputs"]["text"] = prompt
        workflow[KSAMPLER_NODE_ID]["inputs"]["seed"] = random.randint(MIN_SEED, MAX_SEED)

        prompt_id = self._submit_prompt(workflow)

        logger.info("Waiting for ComfyUI result")
        image_info = self._wait_for_result(prompt_id)

        return self._download_image(image_info)

    def _submit_prompt(self, workflow):
        url = f"{self.base_url}/prompt"
        try:
            resp = requests.post(url, json={"prompt": workflow}, timeout=30)
        except requests.RequestException as e:
            raise ComfyUIError(f"failed to submit prompt to ComfyUI: {e}") from e

        if resp.status_code != 200:
            raise ComfyUIError(f"ComfyUI /prompt returned status {resp.status_code}")

        data = resp.json()
        prompt_id = data.get("prompt_id")
        if not prompt_id:
            raise ComfyUIError("ComfyUI /prompt response missing prompt_id")
        return prompt_id

    def _wait_for_result(self, prompt_id):
        url = f"{self.base_url}/history/{prompt_id}"
        deadline = time.monotonic() + self.generation_timeout

        while time.monotonic() < deadline:
            try:
                resp = requests.get(url, timeout=10)
            except requests.RequestException as e:
                raise ComfyUIError(f"failed to poll ComfyUI history: {e}") from e

            if resp.status_code == 200:
                entry = resp.json().get(prompt_id)
                if entry:
                    status = entry.get("status") or {}
                    if status.get("status_str") == "error":
                        raise ComfyUIError(
                            f"ComfyUI reported an execution error for prompt {prompt_id}"
                        )

                    outputs = entry.get("outputs", {})
                    save_node = outputs.get(SAVE_IMAGE_NODE_ID)
                    images = save_node.get("images") if save_node else None

                    if images:
                        image_info = images[0]
                        if "filename" not in image_info:
                            raise ComfyUIError(
                                f"ComfyUI output node {SAVE_IMAGE_NODE_ID} image entry "
                                f"is missing 'filename'"
                            )
                        return image_info

                    # 완료로 표시됐는데도 기대한 output node에 이미지가 없으면
                    # timeout까지 기다릴 이유가 없다 - 바로 실패로 판단한다.
                    if status.get("completed"):
                        raise ComfyUIError(
                            f"ComfyUI marked prompt {prompt_id} completed but output "
                            f"node {SAVE_IMAGE_NODE_ID} produced no image"
                        )

            time.sleep(HISTORY_POLL_INTERVAL_SECONDS)

        raise ComfyUIError(f"ComfyUI generation timed out after {self.generation_timeout}s")

    def _download_image(self, image_info):
        params = {
            "filename": image_info["filename"],
            "subfolder": image_info.get("subfolder", ""),
            "type": image_info.get("type", "output"),
        }
        url = f"{self.base_url}/view"
        try:
            resp = requests.get(url, params=params, timeout=30)
        except requests.RequestException as e:
            raise ComfyUIError(f"failed to download image from ComfyUI: {e}") from e

        if resp.status_code != 200:
            raise ComfyUIError(f"ComfyUI /view returned status {resp.status_code}")

        content_type = resp.headers.get("Content-Type", "image/png")
        return resp.content, image_info["filename"], content_type


def _load_workflow_template():
    with open(WORKFLOW_PATH, "r", encoding="utf-8") as f:
        return json.load(f)
