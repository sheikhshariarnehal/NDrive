import requests
import base64
import uuid

BASE_URL = "http://localhost:3000"
TIMEOUT = 30


def test_get_file_public_stream_with_valid_token():
    upload_url = f"{BASE_URL}/api/upload"
    filename = "testfile_public_stream.txt"
    file_content = b"Test content for public stream with valid token."
    files = {
        "file": (filename, file_content),
        "guest_session_id": (None, "test-guest-session-id")
    }

    # Step 1: Upload file - expect 200 and response with { file: ... }
    try:
        upload_response = requests.post(upload_url, files=files, timeout=TIMEOUT)
        assert upload_response.status_code == 200, f"Unexpected upload status: {upload_response.status_code}"
        upload_json = upload_response.json()
        assert "file" in upload_json, "Response JSON does not contain 'file'"
        file_info = upload_json["file"]
        assert "id" in file_info, "File info missing 'id'"
        assert isinstance(file_info["id"], (str, int)), "'id' should be str or int"
    except requests.RequestException as e:
        assert False, f"Upload request failed: {e}"

    # Construct token: base64url(UUID bytes)
    try:
        file_id_str = str(file_info["id"])
        uuid_obj = uuid.UUID(file_id_str)
    except (ValueError, AttributeError):
        # If the id is not a UUID string, generate a random UUID for token (fallback)
        uuid_obj = uuid.uuid4()
    token_bytes = uuid_obj.bytes
    token_b64url = base64.urlsafe_b64encode(token_bytes).rstrip(b"=").decode("ascii")

    # Step 2: Use token and filename in GET /file/[token]/[filename]
    file_url = f"{BASE_URL}/file/{token_b64url}/{filename}"
    try:
        get_response = requests.get(file_url, allow_redirects=False, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"GET public file stream request failed: {e}"

    # Verify response is a redirect (3xx) with Location header
    assert 300 <= get_response.status_code < 400, f"Expected redirect status 3xx, got {get_response.status_code}"
    location = get_response.headers.get("Location")
    assert location, "Redirect response missing 'Location' header"
    # The location should be a signed URL starting with http/https (basic check)
    assert location.startswith("http"), f"Location header does not appear to be a URL: {location}"


test_get_file_public_stream_with_valid_token()