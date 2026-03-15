import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_post_chunked_upload_complete_with_authorization_success():
    # Sample file metadata and user info for test
    file_name = "testfile.txt"
    file_size = 1024
    mime_type = "text/plain"
    # Authorization token placeholder; in a real test, replace with valid token
    auth_token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fakejwttoken"

    headers_auth = {
        "Authorization": auth_token,
        "Content-Type": "application/json"
    }

    try:
        # Step 1: POST /chunked-upload/init to start chunked upload session
        init_payload = {
            "name": file_name,
            "size_bytes": file_size,
            "mime_type": mime_type
        }
        resp_init = requests.post(
            f"{BASE_URL}/chunked-upload/init",
            json=init_payload,
            headers=headers_auth,
            timeout=TIMEOUT
        )
        assert resp_init.status_code == 201, f"Expected 201 on upload init, got {resp_init.status_code}"
        init_data = resp_init.json()
        assert "upload_session_id" in init_data, "upload_session_id missing in init response"
        assert "expected_chunk_size" in init_data, "expected_chunk_size missing in init response"
        upload_session_id = init_data["upload_session_id"]

        # Step 2: POST /chunked-upload/{upload_session_id} to upload chunk bytes
        chunk_index = 0
        chunk_data = b"a" * file_size  # Simulate a single chunk with 'a's

        headers_chunk = {
            "Authorization": auth_token,
            "Content-Type": "application/octet-stream",
            "Content-Length": str(len(chunk_data)),
            "X-Chunk-Index": str(chunk_index)
        }

        resp_chunk = requests.post(
            f"{BASE_URL}/chunked-upload/{upload_session_id}",
            data=chunk_data,
            headers=headers_chunk,
            timeout=TIMEOUT
        )
        assert resp_chunk.status_code == 200, f"Expected 200 on upload chunk, got {resp_chunk.status_code}"

        # Step 3: POST /chunked-upload/complete with upload_session_id
        complete_payload = {
            "upload_session_id": upload_session_id
        }
        resp_complete = requests.post(
            f"{BASE_URL}/chunked-upload/complete",
            json=complete_payload,
            headers=headers_auth,
            timeout=TIMEOUT
        )
        assert resp_complete.status_code == 200, f"Expected 200 on upload complete, got {resp_complete.status_code}"
        complete_data = resp_complete.json()
        assert "file" in complete_data, "Response missing 'file' key on complete"
        file_obj = complete_data["file"]
        assert "id" in file_obj, "'id' missing in file object"
        assert "telegram_file_id" in file_obj, "'telegram_file_id' missing in file object"
        assert "size_bytes" in file_obj, "'size_bytes' missing in file object"
        assert file_obj["size_bytes"] == file_size, f"size_bytes mismatch, expected {file_size}, got {file_obj['size_bytes']}"

    finally:
        # Cleanup: no delete endpoint specified in PRD
        pass

test_post_chunked_upload_complete_with_authorization_success()
