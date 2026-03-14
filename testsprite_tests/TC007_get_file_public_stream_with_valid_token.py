import base64
import uuid
import requests

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def base64url_encode(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")

def test_get_file_public_stream_with_valid_token():
    # Step 1: Upload a file with POST /api/upload expect 200 and response contains id, telegram_file_id, size_bytes
    upload_url = f"{BASE_URL}/api/upload"
    file_content = b"Hello CloudVault!"
    files = {
        "file": ("testfile.txt", file_content, "text/plain"),
    }
    upload_resp = requests.post(upload_url, files=files, timeout=TIMEOUT)
    assert upload_resp.status_code == 200, f"Expected 200 OK, got {upload_resp.status_code}"
    upload_json = upload_resp.json()
    # Check top-level keys
    assert all(k in upload_json for k in ("id", "telegram_file_id", "size_bytes")), \
        f"Response JSON missing one of required keys: id, telegram_file_id, size_bytes"

    # Extract file id (UUID) and filename
    file_obj = upload_json
    file_id_str = file_obj["id"]
    filename = "testfile.txt"

    try:
        file_uuid = uuid.UUID(file_id_str)
    except Exception as e:
        assert False, f"File id is not valid UUID: {file_id_str}"

    # Step 2: Build token as base64url encoded UUID bytes and filename as param
    token = base64url_encode(file_uuid.bytes)

    # Step 3: Test GET /file/{token}/{filename}, expect redirect (30x) and Location header
    get_url = f"{BASE_URL}/file/{token}/{filename}"
    get_resp = requests.get(get_url, allow_redirects=False, timeout=TIMEOUT)

    # Verify response is a redirect (status code 300-399)
    assert 300 <= get_resp.status_code < 400, \
        f"Expected redirect status code 30x, got {get_resp.status_code}"

    # Verify Location header exists and is a non-empty string
    location = get_resp.headers.get("Location")
    assert location and isinstance(location, str), "Redirect response missing valid Location header"

    # Additional basic check: Location should be a URL (starts with http or https)
    assert location.startswith("http"), f"Location header does not appear to be a URL: {location}"

    # No need for explicit cleanup as the requirement does not provide delete endpoint for files

test_get_file_public_stream_with_valid_token()
