import requests
import uuid
import base64
import io

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_get_file_public_stream_with_range_header_partial_content():
    upload_url = f"{BASE_URL}/api/upload"
    guest_session_id = str(uuid.uuid4())
    filename = "testfile.txt"
    file_content = b"Hello CloudVault! This is a test file content for partial content streaming."

    files = {
        "file": (filename, io.BytesIO(file_content)),
    }
    data = {
        "guest_session_id": guest_session_id,
    }

    # Upload file expecting 200 with { file: ... }
    try:
        upload_response = requests.post(upload_url, files=files, data=data, timeout=TIMEOUT)
        assert upload_response.status_code == 200, f"Expected 200, got {upload_response.status_code}"
        json_resp = upload_response.json()
        assert "file" in json_resp, "Response JSON missing 'file' key"
        file_meta = json_resp["file"]
        assert "id" in file_meta and "telegram_file_id" in file_meta and "size_bytes" in file_meta, \
            "File metadata keys missing in response"

        # Build token as base64url(UUID bytes)
        file_id_str = file_meta["id"]
        # Assuming file_meta["id"] is a UUID string; parse bytes from UUID
        file_uuid = uuid.UUID(file_id_str)
        token = base64.urlsafe_b64encode(file_uuid.bytes).decode("utf-8").rstrip("=")

        # Perform GET /file/{token}/{filename} with Range header bytes=0-59 (valid range for file content length)
        get_url = f"{BASE_URL}/file/{token}/{filename}"
        headers = {
            "Range": "bytes=0-59"
        }
        get_response = requests.get(get_url, headers=headers, timeout=TIMEOUT)
        assert get_response.status_code == 206, f"Expected 206 Partial Content, got {get_response.status_code}"
        assert "Content-Range" in get_response.headers, "Content-Range header missing in response"
        content_range = get_response.headers["Content-Range"]
        # Content-Range should reflect bytes 0-59 (length 60)
        assert content_range.startswith("bytes 0-"), f"Unexpected Content-Range header value: {content_range}"

        # Response content length should be <= 60 bytes (range size)
        content_length = int(get_response.headers.get("Content-Length", 0))
        assert 0 < content_length <= 60, "Content-Length header invalid or too large for requested range"

    finally:
        # Cleanup: no delete endpoint specified in PRD, so skip cleanup.
        pass

test_get_file_public_stream_with_range_header_partial_content()
