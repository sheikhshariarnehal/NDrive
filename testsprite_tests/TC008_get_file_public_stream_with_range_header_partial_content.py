import base64
import requests
import uuid

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_get_file_public_stream_with_range_header_partial_content():
    # Step 1: Upload a file to get a valid file metadata
    files = {"file": ("testfile.txt", b"Test file content for range request" * 100, "text/plain")}
    data = {"guest_session_id": "test-guest-session-range"}
    upload_resp = requests.post(f"{BASE_URL}/api/upload", files=files, data=data, timeout=TIMEOUT)
    assert upload_resp.status_code == 201
    upload_data = upload_resp.json()
    assert "file" in upload_data and isinstance(upload_data["file"], dict)
    file_id = upload_data["file"].get("id")
    file_size = upload_data["file"].get("size_bytes")
    filename = "testfile.txt"
    assert file_id is not None and file_size is not None

    # Token is base64url encoded UUID bytes
    token = base64.urlsafe_b64encode(uuid.UUID(file_id).bytes).rstrip(b"=").decode("ascii")

    # Step 3: Request public file route with Range header
    headers = {
        "Range": "bytes=0-1023"
    }
    range_resp = requests.get(f"{BASE_URL}/file/{token}/{filename}", headers=headers, timeout=TIMEOUT, stream=True)
    assert range_resp.status_code == 206, f"Expected 206 Partial Content but got {range_resp.status_code}"
    content_range = range_resp.headers.get("Content-Range")
    assert content_range is not None, "Content-Range header missing in partial content response"


test_get_file_public_stream_with_range_header_partial_content()
