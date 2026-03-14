import base64
import requests
import uuid

BASE_URL = "http://localhost:3000"
TIMEOUT = 30

def test_get_file_public_stream_with_valid_token():
    # Step 1: Upload a file to obtain file UUID and filename
    upload_url = f"{BASE_URL}/api/upload"
    test_file_content = b"Test file content for TC007"
    test_filename = "tc007_test_file.txt"
    files = {"file": (test_filename, test_file_content)}
    data = {"guest_session_id": "test-guest-session"}
    response = requests.post(upload_url, files=files, data=data, timeout=TIMEOUT)
    assert response.status_code == 201, f"Upload failed with status {response.status_code}: {response.text}"
    data = response.json()
    assert "file" in data and isinstance(data["file"], dict), "Response JSON missing file object"
    file_id = data["file"].get("id")
    assert isinstance(file_id, str), "file.id is not a string"

    # The token is base64url encoding of UUID (file id)
    # Validate UUID format
    try:
        file_uuid = uuid.UUID(file_id)
    except ValueError:
        raise AssertionError(f"file.id is not a valid UUID: {file_id}")

    # Base64url encode the UUID bytes (16 bytes)
    token = base64.urlsafe_b64encode(file_uuid.bytes).rstrip(b"=").decode("ascii")

    # Compose the GET /file/[token]/filename path
    file_path = f"/file/{token}/{test_filename}"
    get_url = BASE_URL + file_path

    try:
        # Step 2: Access the public file stream with the valid token
        resp = requests.get(get_url, allow_redirects=False, timeout=TIMEOUT)
        # Verify status is redirect (3xx)
        assert resp.status_code >= 300 and resp.status_code < 400, \
            f"Expected 3xx redirect status, got {resp.status_code}"
        # Verify Location header exists and is a signed TDLib URL (basic check for presence)
        location = resp.headers.get("Location")
        assert location, "Location header missing in redirect response"
        # Basic validation: Location should be a URL (http or https)
        assert location.startswith("http://") or location.startswith("https://"), \
            f"Location header does not start with http:// or https://: {location}"

    finally:
        # Cleanup: delete the uploaded file if API supports it (not specified in PRD, so omitted)
        pass

test_get_file_public_stream_with_valid_token()
