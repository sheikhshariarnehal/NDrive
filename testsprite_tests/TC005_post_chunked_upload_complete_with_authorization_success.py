import requests
import uuid

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_post_chunked_upload_complete_with_authorization_success():
    guest_session_id = str(uuid.uuid4())
    file_name = f"chunked-{uuid.uuid4().hex}.txt"
    file_content = b"This is a chunked upload test." * 20
    total_chunks = 3

    # Step 1: init via frontend proxy route
    init_payload = {
        "fileName": file_name,
        "fileSize": len(file_content),
        "mimeType": "text/plain",
        "totalChunks": total_chunks,
        "guestSessionId": guest_session_id,
    }
    init_resp = requests.post(
        f"{BASE_URL}/api/upload/init",
        json=init_payload,
        timeout=TIMEOUT,
    )
    assert init_resp.status_code == 200, f"Init failed: {init_resp.status_code} {init_resp.text}"
    init_data = init_resp.json()
    upload_id = init_data.get("uploadId")
    assert isinstance(upload_id, str) and upload_id, "uploadId missing from init response"

    # Step 2: upload chunks through frontend proxy route
    chunk_size = len(file_content) // total_chunks
    chunks = [file_content[i * chunk_size : (i + 1) * chunk_size] for i in range(total_chunks - 1)]
    chunks.append(file_content[(total_chunks - 1) * chunk_size :])

    for idx, chunk_data in enumerate(chunks):
        files = {"chunk": (f"chunk-{idx}.bin", chunk_data, "application/octet-stream")}
        data = {"uploadId": upload_id, "chunkIndex": str(idx)}
        chunk_resp = requests.post(
            f"{BASE_URL}/api/upload/chunk",
            files=files,
            data=data,
            timeout=TIMEOUT,
        )
        assert chunk_resp.status_code == 200, f"Chunk {idx} failed: {chunk_resp.status_code} {chunk_resp.text}"

    # Step 3: complete via frontend proxy route
    complete_payload = {
        "uploadId": upload_id,
        "fileName": file_name,
        "fileSize": len(file_content),
        "mimeType": "text/plain",
        "guestSessionId": guest_session_id,
    }
    complete_resp = requests.post(
        f"{BASE_URL}/api/upload/complete",
        json=complete_payload,
        timeout=TIMEOUT,
    )
    assert complete_resp.status_code == 201, f"Complete failed: {complete_resp.status_code} {complete_resp.text}"
    complete_data = complete_resp.json()
    file_info = complete_data.get("file")
    assert isinstance(file_info, dict), "Response missing file object"
    assert isinstance(file_info.get("id"), str) and file_info["id"], "Missing file.id"
    assert isinstance(file_info.get("telegram_file_id"), str) and file_info["telegram_file_id"], "Missing telegram_file_id"
    assert isinstance(file_info.get("size_bytes"), int) and file_info["size_bytes"] == len(file_content), "size_bytes mismatch"


test_post_chunked_upload_complete_with_authorization_success()
