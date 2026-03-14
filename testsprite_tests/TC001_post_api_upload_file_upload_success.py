import requests
from requests.exceptions import RequestException

BASE_URL = "http://localhost:3000"
TIMEOUT = 30


def test_post_api_upload_file_upload_success():
    url = f"{BASE_URL}/api/upload"
    guest_session_id = "guest-session-test-12345"
    # Prepare file content and multipart data
    file_content = b"Test file content for upload success"
    files = {
        "file": ("testfile.txt", file_content, "text/plain"),
    }
    data = {
        "guest_session_id": guest_session_id,
    }

    try:
        response = requests.post(url, data=data, files=files, timeout=TIMEOUT)
        assert response.status_code == 201, f"Expected status 201, got {response.status_code}"
        json_resp = response.json()
        assert "file" in json_resp, "Response JSON missing 'file' key"
        file_meta = json_resp["file"]
        assert isinstance(file_meta, dict), "'file' should be a dict"
        assert "id" in file_meta and file_meta["id"], "'id' missing or empty in file metadata"
        assert "telegram_file_id" in file_meta and file_meta["telegram_file_id"], "'telegram_file_id' missing or empty"
        assert "size_bytes" in file_meta and isinstance(file_meta["size_bytes"], int), "'size_bytes' missing or not int"
    except RequestException as e:
        assert False, f"RequestException during upload: {e}"


test_post_api_upload_file_upload_success()