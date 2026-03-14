import requests
import io

def test_post_api_upload_file_upload_success():
    base_url = "http://localhost:3000"
    endpoint = "/api/upload"
    url = base_url + endpoint
    timeout = 30
    guest_session_id = "guest-session-test-123"

    # Prepare a small in-memory file to upload
    file_content = b"Hello, this is a test file for upload."
    file_name = "test_upload_file.txt"
    files = {
        'file': (file_name, io.BytesIO(file_content), 'text/plain')
    }
    data = {
        'guest_session_id': guest_session_id
    }

    try:
        response = requests.post(url, files=files, data=data, timeout=timeout)
        assert response.status_code == 201, f"Expected status 201, got {response.status_code}"
        resp_json = response.json()
        assert 'file' in resp_json, "Response JSON missing 'file' key"
        file_obj = resp_json['file']
        assert isinstance(file_obj, dict), "'file' key should be a dictionary"
        # Validate required keys in file object
        for key in ('id', 'telegram_file_id', 'size_bytes'):
            assert key in file_obj, f"Missing key '{key}' in file object"
        # Validate size_bytes approximately matches upload size
        assert file_obj['size_bytes'] == len(file_content), "size_bytes does not match uploaded file size"
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

test_post_api_upload_file_upload_success()