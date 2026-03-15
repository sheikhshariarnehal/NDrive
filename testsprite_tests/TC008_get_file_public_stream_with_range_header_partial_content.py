import base64
import uuid
import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_get_file_public_stream_with_range_header_partial_content():
    guest_session_id = str(uuid.uuid4())
    file_content = b"Hello, this is a test file content for TC008." * 50  # ~2000 bytes
    filename = "test_file_tc008.txt"
    upload_url = f"{BASE_URL}/api/upload"

    # Upload file with guest_session_id
    files = {
        "file": (filename, file_content, "text/plain")
    }
    data = {
        "guest_session_id": guest_session_id
    }

    response_upload = requests.post(upload_url, data=data, files=files, timeout=TIMEOUT)
    assert response_upload.status_code == 201, f"Expected 201 on upload, got {response_upload.status_code}"
    json_upload = response_upload.json()
    assert "file" in json_upload, "Response JSON missing 'file' key"
    file_meta = json_upload["file"]

    # Use file id as UUID for token generation or telegram_file_id if UUID format not given
    # The test instruction says build token as base64url(UUID bytes)
    # Assume file_meta["id"] is UUID string, else fallback to generated UUID for test
    try:
        file_uuid = uuid.UUID(file_meta["id"])
    except Exception:
        file_uuid = uuid.uuid4()

    # base64url encode UUID bytes (without padding)
    token = base64.urlsafe_b64encode(file_uuid.bytes).rstrip(b"=").decode("ascii")

    file_name_for_fetch = filename
    file_url = f"{BASE_URL}/file/{token}/{file_name_for_fetch}"
    headers = {
        "Range": "bytes=0-1023"
    }

    # Perform GET request with Range header
    response_get = requests.get(file_url, headers=headers, timeout=TIMEOUT)
    # Expect 206 Partial Content
    assert response_get.status_code == 206, f"Expected 206 Partial Content, got {response_get.status_code}"
    # Validate Content-Range header presence and correctness
    content_range = response_get.headers.get("Content-Range")
    assert content_range is not None, "Missing Content-Range header in partial content response"
    # Content-Range format: bytes 0-1023/total or bytes 0-1023/*
    assert content_range.startswith("bytes "), f"Content-Range does not start with 'bytes ': {content_range}"

    # Content length should be <= 1024 bytes
    assert len(response_get.content) <= 1024, f"Partial content length {len(response_get.content)} exceeds range requested"

test_get_file_public_stream_with_range_header_partial_content()